/*
 * utils.h
 *
 *  Created on: Feb 12, 2017
 *      Author: karl
 */

#ifndef SRC_GEN_OPS_UTILS_H_
#define SRC_GEN_OPS_UTILS_H_

#include <ctype.h>

namespace tensorflow {

namespace {

static std::string __reserved_keywords[] = {
  "switch", "assert", "const"
}; // TODO add more

static std::string __reserved_classes[] = {
  "Shape"
}; // TODO add more


inline string FromUnderscoreToOtherCase(const std::string& str, bool startWithCap) {
  std::string result;
  const char joiner = '_';
  std::size_t i = 0;
  bool cap = startWithCap;
  while (i < str.size()) {
    const char c = str[i++];
    if (c == joiner) {
      cap = true;
    } else if (cap) {
      result += toupper(c);
      cap = false;
    } else {
      result += c;
    }
  }
  return result;
}

}

inline std::string GetLastPath(const std::string& fname) {
  auto pos = fname.rfind("/");
  return fname.substr(pos + 1);
}

inline string FromUnderscoreToPascalCase(const std::string& str) {
  return FromUnderscoreToOtherCase(str, true);
}

inline string FromUnderscoreToCamelCase(const std::string& str) {
  return FromUnderscoreToOtherCase(str, false);
}

inline std::string FromPascalToCamelCase(const std::string& str) {
  std::string result = str;
  result.replace(0, 1, 1, tolower(str[0]));
  return result;
}

inline std::string ProtectReservedKeywords(const std::string& str, const std::string& suffix) {
  if (std::find(std::begin(__reserved_keywords), std::end(__reserved_keywords), str) != std::end(__reserved_keywords)) {
    return str + suffix;
  }
  return str;
}

inline std::string ProtectReservedClasses(const std::string& str, const std::string& suffix) {
  if (std::find(std::begin(__reserved_classes), std::end(__reserved_classes), str) != std::end(__reserved_classes)) {
    return str + suffix;
  }
  return str;
}


} /* namespace tensorflow */

#endif /* SRC_GEN_OPS_UTILS_H_ */
