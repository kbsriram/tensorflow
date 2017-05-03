/*
 * template.cpp
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#include "tensorflow/core/platform/env.h"
#include "template.h"

#define TEMPLATE_PATH "/home/kbsriram/extsrc/tensorflow/tensorflow/java/src/gen/ops/templates/" // FIXME
#define TEMPLATE_EXT ".tmpl"

using namespace std;

namespace tensorflow {

Template::Template() {
  this->env = NULL;
}

Template::~Template() {}

void Template::Load(Env* env, const string& name) {
  this->env = env;
  this->name = name;
  TF_CHECK_OK(ReadFileToString(env, TEMPLATE_PATH + name + TEMPLATE_EXT, &template_src));
}

string Template::Render(const map<string, string>& params) {
  StringWriter writer;
  RenderWith(writer, params);
  return writer.str();
}

void Template::RenderToFile(const string& fname, const map<string, string>& params) {
  FileWriter writer(env, fname);
  RenderWith(writer, params);
}

void Template::RenderWith(Writer& writer, const map<string, string>& params) {
  size_t pos = 0;
  while (pos < template_src.size()) {
    size_t next_param_pos = template_src.find("${", pos); // could end up with npos
    writer.Append(template_src.substr(pos, next_param_pos - pos));
    if (next_param_pos == string::npos) {
      break;
    }
    next_param_pos += 2;
    pos = template_src.find('}', next_param_pos);
    string param_name = template_src.substr(next_param_pos, pos - next_param_pos);
    map<string, string>::const_iterator param = params.find(param_name);
    if (param != params.end()) {
      writer.Append(param->second);
    }
    ++pos;
  }
}

}
