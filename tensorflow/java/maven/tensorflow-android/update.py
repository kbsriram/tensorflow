#  Copyright 2017 The TensorFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""Update android pom properties from CI build."""

import argparse
import json
import string
import sys
import urllib2


def get_args():
  parser = argparse.ArgumentParser()
  parser.add_argument(
      '--version', required=True, help='Version for the artifact.')
  parser.add_argument(
      '--dir',
      required=True,
      help='Directory where the pom and aar artifact will be written.')
  parser.add_argument(
      '--template', required=True, help='Path to pom template file.')
  return parser.parse_args()


def get_json(url):
  url = '%s/api/json' % url
  return json.load(urllib2.urlopen(url))


def get_commit_id(build_info):
  actions = build_info.get('actions')
  build_data = next(
      a for a in actions
      if a.get('_class') == 'hudson.plugins.git.util.BuildData')
  if not build_data:
    raise ValueError('Missing BuildData: %s' % build_info)
  revision_info = build_data.get('lastBuiltRevision')
  if not revision_info:
    raise ValueError('Missing lastBuiltRevision: %s' % build_info)
  return revision_info.get('SHA1')


def get_aar_url(build_info):
  """Given the root build info, find the URL to the tensorflow.aar artifact."""
  base_url = build_info.get('url')
  if not base_url:
    raise ValueError('Missing url: %s' % build_info)
  build_class = build_info.get('_class')
  if (build_class == 'hudson.model.FreeStyleBuild' or
      build_class == 'hudson.matrix.MatrixRun'):
    # Artifacts are directly available from this buildinfo class
    aar_info = next(
        a for a in build_info.get('artifacts')
        if a.get('fileName') == 'tensorflow.aar')
    if not aar_info:
      raise ValueError('Missing aar artifact: %s' % build_info)
    return '%s/artifact/%s' % (base_url, aar_info.get('relativePath'))

  if build_class == 'hudson.matrix.MatrixBuild':
    # Artifacts are available from its build number info url.
    target = build_info.get('number')
    if not target:
      raise ValueError('Missing build_number: %s' % build_info)
    # Fetch the actual URL by sweeping through the runs
    run_url = next(
        r for r in build_info.get('runs') if r.get('number') == target)
    if not run_url:
      raise ValueError('Missing run_url for %d: %s' % (target, build_info))
    run_info = get_json(run_url.get('url'))
    return get_aar_url(run_info)

  raise ValueError('Unknown build_type %s' % build_info)


def read_template(path):
  with open(path) as f:
    return string.Template(f.read())


def main():
  args = get_args()

  # Artifacts are downloaded from the ci build. A SNAPSHOT release is
  # associated with artifacts from the last successful nightly build. Otherwise,
  # it comes from the last successful Release build.
  if args.version.endswith('SNAPSHOT'):
    info_url = 'Nightly/job/nightly-android'
    build_type = 'nightly-android'
  else:
    info_url = 'Release/job/release-matrix-android'
    build_type = 'release-matrix-android'

  info_url = 'https://ci.tensorflow.org/view/%s/lastSuccessfulBuild' % info_url
  # Fetch the json file
  build_info = get_json(info_url)

  # Check all required build info is present
  if build_info.get('result') != 'SUCCESS':
    raise ValueError('Invalid json: %s' % build_info)
  build_url = build_info.get('url')
  if not build_url:
    raise ValueError('Missing url: %s' % build_info)
  build_number = build_info.get('number')
  if not build_number:
    raise ValueError('Missing build number: %s' % build_info)
  build_commit_id = get_commit_id(build_info)
  if not build_commit_id:
    raise ValueError('Missing commit id: %s' % build_info)

  # Write the pom file updated with build attributes.
  template = read_template(args.template)
  with open('%s/pom-android.xml' % args.dir, 'w') as f:
    f.write(
        template.substitute({
            'build_commit_id': build_commit_id,
            'build_number': build_number,
            'build_type': build_type,
            'build_url': build_url,
            'version': args.version
        }))

  # Download the aar file for this build.
  with open('%s/tensorflow.aar' % args.dir, 'w') as f:
    aar = urllib2.urlopen(get_aar_url(build_info))
    f.write(aar.read())


if __name__ == '__main__':
  sys.exit(main())
