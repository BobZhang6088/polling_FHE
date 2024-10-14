cmake_minimum_required(VERSION 3.10)
project(CppService)

set(CMAKE_CXX_STANDARD 17)

# 设置 Microsoft SEAL 路径
find_package(SEAL REQUIRED)

# 查找 nlohmann/json 库
find_package(nlohmann_json REQUIRED)

# 添加 cpp-httplib
add_subdirectory(httplib)

# 包含本地头文件目录
include_directories(${CMAKE_SOURCE_DIR})

# 构建应用，将 main.cpp 和 base64.cpp 添加到可执行文件中
add_executable(cpp-service main.cpp base64.cpp)

# 链接依赖库
target_link_libraries(cpp-service SEAL::seal httplib nlohmann_json::nlohmann_json)
