cmake_minimum_required(VERSION 3.10)
project(CppService)

set(CMAKE_CXX_STANDARD 17)

# 设置 Microsoft SEAL 路径
find_package(SEAL REQUIRED)
include_directories(${SEAL_INCLUDE_DIR})

# 添加 cpp-httplib
add_subdirectory(httplib)

# 包含 nlohmann/json
include_directories(/usr/local/include)

# 构建应用
add_executable(cpp-service main.cpp)

target_link_libraries(cpp-service SEAL::seal httplib)
