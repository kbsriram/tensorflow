/*
 * template.h
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#ifndef SRC_GEN_OPS_TEMPLATE_H_
#define SRC_GEN_OPS_TEMPLATE_H_

#include <string>

#include "tensorflow/core/platform/env.h"
#include "writer.h"

namespace tensorflow {

class Template {

public:
  Template();
  virtual ~Template();

  void Load(Env* env, const std::string& name);
  string Render(const std::map<std::string, std::string>& params);
  void RenderToFile(const string& fname, const std::map<std::string, std::string>& params);

private:
  Env* env;
  string name;
  string template_src;

  void RenderWith(Writer& writer, const std::map<string, string>& params);
};

}

#endif /* SRC_GEN_OPS_TEMPLATE_H_ */
