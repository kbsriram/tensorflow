/*
 * type_evaluator.h
 *
 *  Created on: Mar 6, 2017
 *      Author: karl
 */

#ifndef SRC_GEN_OPS_TYPES_H_
#define SRC_GEN_OPS_TYPES_H_

#include <string>
#include <set>
#include <map>

#include "tensorflow/core/framework/op.h"

namespace tensorflow {

class Type {

public:
  Type();
  Type(const std::string& java_type);
  Type(const std::string& java_type, const std::string& java_import);
  Type(const Type& type);
  virtual ~Type() {}

  const std::string& JavaType() const { return java_type_; }
  const std::string& JavaImport() const { return java_import_; }
  bool Inferred() const { return inferred_; }
  bool Undefined() const { return java_type_.empty(); }

private:
  std::string java_type_;
  std::string java_import_;
  bool inferred_ = false;

  friend class TypeEvaluator;
};

class TypeEvaluator {

public:
  TypeEvaluator(const OpDef& op);
  virtual ~TypeEvaluator();

  const Type TypeOf(const OpDef_ArgDef& arg);
  const Type TypeOf(const OpDef_AttrDef& attr);

private:
  std::map<const std::string, Type> attr_types_;

  void EvalAttrTypes(const OpDef& op);
};

} /* namespace tensorflow */

#endif /* SRC_GEN_OPS_TYPES_H_ */
