/*
 * op_generator.cpp
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#include "generator.h"
#include "tensorflow/core/framework/op.h"
#include "tensorflow/core/platform/logging.h"
#include "utils.h"

using namespace std;

namespace tensorflow {

OpGenerator::OpGenerator(const string& lib_fname) :
  lib_name(GetLastPath(lib_fname)),
  pkg_name(lib_name.substr(0, lib_name.rfind("_op"))),
  file_path(lib_name + "/org/tensorflow/ops/" + pkg_name + "/") {}

OpGenerator::~OpGenerator() {}

void OpGenerator::Run(Env* env, bool include_internal) {
  LoadTemplates(env);

  LOG(INFO) << "Generating files for op library <" << lib_name << ">" << endl;
  if (!env->FileExists(file_path).ok()) {
    TF_CHECK_OK(env->RecursivelyCreateDir(file_path));
  }
  OpList ops;
  OpRegistry::Global()->Export(include_internal, &ops);
  WriteOps(ops);
}

void OpGenerator::LoadTemplates(Env* env) {
  // template_lib.Load(env, "lib");
  // template_lib_ops.Load(env, "lib_ops");
  template_op.Load(env, "op");
  template_op_attrs.Load(env, "op_attrs");
  template_op_outputs.Load(env, "op_outputs");
  template_op_output_list_setter.Load(env, "op_output_list_setter");
  template_op_output_setter.Load(env, "op_output_setter");
  template_op_implement_method.Load(env, "op_implement_method");
}

void OpGenerator::WriteOps(const OpList& ops) {
  map<string, string> params;
  string lib_ops;
  string lib_name_pc = FromUnderscoreToPascalCase(lib_name);

  params["lib_name_pc"] = lib_name_pc;
  params["lib_name_cc"] = FromPascalToCamelCase(lib_name_pc);
  params["pkg_name"] = pkg_name;
  params["imports"] = "";

  for (const auto& op : ops.op()) {
    //if (op.name() == "Const" || op.name() == "NoOp") {
    //  continue; // special case: this operation is handled programmatically
    //}
    WriteOp(op, params);
    // lib_ops += template_lib_ops.Render(params);
  }
  // params["lib_ops"] = lib_ops;

  // template_lib.RenderToFile(ToFileName(lib_name_pc), params);
}

void OpGenerator::WriteOp(const OpDef& op, map<string, string>& params) {
  LOG(INFO) << "----- Writing " << pkg_name << "/" << op.name() << endl;
  string op_name_pc = ProtectReservedClasses(op.name(), "Op");
  params["op_name_pc"] = op_name_pc;
  params["op_name_cc"] = ProtectReservedKeywords(FromPascalToCamelCase(op.name()), "Op");
  params["op_name_desc"] = ReplaceAll(
      op.summary() + "\n\n" + op.description(), "\n", "\n// ");

  string op_inputs, op_inputs_builder, op_params, op_attrs, op_mandatory_attrs, op_outputs, op_output_setter;
  TypeEvaluator type_evaluator(op);

  // Write operation inputs
  for (const OpDef_ArgDef& arg : op.input_arg()) {
    Type type = type_evaluator.TypeOf(arg);

    if (ImportType(type, params)) {
      string arg_name_cc = FromUnderscoreToCamelCase(arg.name());
      op_inputs += ", " + type.JavaType() + ' ' + arg_name_cc;
      op_inputs_builder += "\n        ";
      if (type_evaluator.ArgIsList(arg)) {
        op_inputs_builder +=
            ".addInputList(" + arg_name_cc + ".inputList())";
      } else {
        op_inputs_builder +=
            ".addInput(" + arg_name_cc + ".input())";
      }
    }
  }

  // Write operation outputs
  string output_name_cc;
  string output_type;
  for (const OpDef_ArgDef& arg : op.output_arg()) {
    const Type type = type_evaluator.TypeOf(arg);

    if (ImportType(type, params)) {
      output_name_cc = FromUnderscoreToCamelCase(arg.name());
      output_type = type.JavaType();

      params["output_name_cc"] = output_name_cc;
      params["output_type"] = output_type;
      if (type_evaluator.ArgIsList(arg)) {
        params["output_is_array"] = "[]";
        op_output_setter += template_op_output_list_setter.Render(params);
      } else {
        params["output_is_array"] = "";
        op_output_setter += template_op_output_setter.Render(params);
      }
      op_outputs += template_op_outputs.Render(params);
    }
  }

  params["op_implement_method"] = "";
  params["op_implements"] = "";
  if (op.output_arg().size() == 1) {
    // have the class implement the output class, and implement
    // the appropriate method if necessary.
    params["op_implements"] = "implements " + output_type;
    if (output_type == "InputSource" || output_type == "VariableInputSource") {
      if (output_name_cc != "input") {
        params["op_method_name"] = "input";
        params["op_implement_method"] = template_op_implement_method.Render(params);
      }
    } else if (output_type == "InputListSource"
               || output_type == "VariableListInputSource") {
      if (output_name_cc != "inputList") {
        params["op_method_name"] = "inputList";
        params["op_implement_method"] = template_op_implement_method.Render(params);
      }
    }
  }

  // Write operation attributes
  for (const OpDef_AttrDef& attr : op.attr()) {
    const Type type = type_evaluator.TypeOf(attr);

    if (!type.Inferred() && ImportType(type, params)) {
      string attr_name_cc = FromUnderscoreToCamelCase(attr.name());
      string attr_name_pc = FromUnderscoreToPascalCase(attr.name());

      params["attr_name"] = attr.name();
      params["attr_name_pc"] = attr_name_pc;
      params["attr_type"] = type.JavaType();
      op_attrs += template_op_attrs.Render(params);


      if (!attr.has_default_value()) {
        op_params += ", " + type.JavaType() + ' ' + attr_name_cc;
        op_mandatory_attrs += ".with" + attr_name_pc + "(" + attr_name_cc + ")";
      }
    }

  }

  params["op_inputs"] = op_inputs;
  params["op_inputs_builder"] = op_inputs_builder;
  params["op_params"] = op_params;
  params["op_attrs"] = op_attrs;
  params["op_mandatory_attrs"] = op_mandatory_attrs;
  params["op_outputs"] = op_outputs;
  params["op_output_setter"] = op_output_setter;

  template_op.RenderToFile(ToFileName(op_name_pc),  params);
}

bool OpGenerator::ImportType(const Type& type, std::map<std::string, std::string>& params) {
  if (!type.Undefined()) {
    if (!type.JavaImport().empty()) {
      string import = "import " + type.JavaImport() + ";\n";
      if (params["imports"].find(import) == string::npos) {
        params["imports"].append(import);
      }
    }
    return true;
  }
  return false;
}

string OpGenerator::ReplaceAll(const string& in, const string& find, const string& replace) {
  string newString;
  newString.reserve(in.length());  // avoids a few memory allocations

  string::size_type lastPos = 0;
  string::size_type findPos;

  while(string::npos != (findPos = in.find(find, lastPos))) {
    newString.append(in, lastPos, findPos - lastPos);
    newString += replace;
    lastPos = findPos + find.length();
  }

  // Care for the rest after last occurrence
  newString += in.substr(lastPos);
  return newString;
}

} // namespace tensorflow
