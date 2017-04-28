/*
 * type_evaluator.cpp
 *
 *  Created on: Mar 6, 2017
 *      Author: karl
 */
#include <cstring>

#include "tensorflow/core/platform/logging.h"
#include "types.h"

using namespace ::google::protobuf;

namespace tensorflow {

Type::Type() {
}

Type::Type(const std::string& java_type)
  : java_type_(java_type) {
}

Type::Type(const std::string& java_type, const std::string& java_import)
  : java_type_(java_type),
    java_import_(java_import) {
}

Type::Type(const Type& type)
  : java_type_(type.java_type_),
    java_import_(type.java_import_),
    inferred_(type.inferred_) {
}

TypeEvaluator::TypeEvaluator(const OpDef& op) {
  EvalAttrTypes(op);
}

TypeEvaluator::~TypeEvaluator() {
}

const Type TypeEvaluator::TypeOf(const OpDef_ArgDef& arg) {
  // TODO! Add support for InputSource arrays when arg.number_attr() is present!
  return Type("InputSource", "org.tensorflow.InputSource");
}

const Type TypeEvaluator::TypeOf(const OpDef_AttrDef& attr) {
    return attr_types_.at(attr.name());
}

const map<string, const Type> kAttrTypeMap = {
    {"type", Type("DataType", "org.tensorflow.DataType")},
    {"string", Type("String")},
    {"int", Type("long")},
    {"float", Type("float")},
    {"bool", Type("boolean")},
    {"list(type)", Type("DataType[]", "org.tensorflow.DataType")},
    {"list(int)", Type("long[]")},
    {"list(float)", Type("float[]")},
    {"list(bool)", Type("boolean[]")}
};

void TypeEvaluator::EvalAttrTypes(const OpDef& op) {
  for (const OpDef_AttrDef& attr : op.attr()) {
    Type attr_type;

    auto t = kAttrTypeMap.find(attr.type());
    if (t != kAttrTypeMap.end()) {
      attr_type = t->second;

      // Check whether this attribute is a type inferred by an input argument
      for (const OpDef_ArgDef& arg : op.input_arg()) {
        if (arg.type_attr() == attr.name() || arg.number_attr() == attr.name()) {
          attr_type.inferred_ = true;
          break;
        }
      }

    } else {
      LOG(WARNING) << "Unsupported attribute type \"" << attr.type() << "\"" << endl;
    }

    attr_types_.insert(std::pair<const string, Type>(attr.name(), attr_type));
  }
}

} /* namespace tensorflow */
