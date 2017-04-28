/*
 * op_generator.h
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#ifndef SRC_GEN_OPS_GENERATOR_H_
#define SRC_GEN_OPS_GENERATOR_H_

#include "tensorflow/core/framework/op_def.pb.h"
#include "template.h"
#include "types.h"

namespace tensorflow {

class OpGenerator {

public:
  OpGenerator(const std::string& lib_fname);
  virtual ~OpGenerator();

  void Run(Env* env, bool include_internal);

private:
  const std::string lib_name;
  const std::string pkg_name;
  const std::string file_path;
  Template template_lib;
  Template template_lib_ops;
  Template template_op;
  Template template_op_attrs;

  void LoadTemplates(Env* env);
  void WriteOps(const OpList& ops);
  void WriteOp(const OpDef& op, std::map<string, string>& params);
  const std::string ConvertTypeToJava(const std::string& type);
  const std::string ConvertTypeToJava(int ctype);
  bool ImportType(const Type& type, std::map<std::string, std::string>& params);

  inline string ToFileName(string base_name) {
    return file_path + base_name + ".java";
  }

};

} // namespace tensorflow

#endif /* SRC_GEN_OPS_GENERATOR_H_ */
