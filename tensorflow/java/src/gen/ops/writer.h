/*
 * template.h
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#ifndef SRC_GEN_OPS_WRITER_H_
#define SRC_GEN_OPS_WRITER_H_

#include <string>

#include "tensorflow/core/platform/env.h"

namespace tensorflow {

class Writer {

public:
  Writer() {}
  virtual ~Writer() {}

  virtual void Append(const std::string& str) = 0;
};

class StringWriter : public Writer {

public:
  StringWriter() {}
  virtual ~StringWriter() {}

  void Append(const std::string& str) {
    ostream << str;
  }

  std::string str() {
    return ostream.str();
  }

private:
  std::ostringstream ostream;
};

class FileWriter : public Writer {

public:
  FileWriter(Env* env, const std::string& fname) {
    TF_CHECK_OK(env->NewWritableFile(fname, &ofile));
  }
  virtual ~FileWriter() {
    TF_CHECK_OK(ofile->Close());
  }

  void Append(const std::string& str) {
    TF_CHECK_OK(ofile->Append(str));
  }

private:
  std::unique_ptr<WritableFile> ofile;
};

}

#endif /* SRC_GEN_OPS_WRITER_H_ */
