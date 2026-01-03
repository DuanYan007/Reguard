import asyncio
import argparse
from mcp.server.fastmcp import FastMCP
from mcp.server import Server
from starlette.applications import Starlette
from starlette.routing import Route
from mcp.server.streamable_http_manager import StreamableHTTPSessionManager
import uvicorn
from .converters import convert, get_supported_formats


# 初始化 FastMCP 服务器
mcp = FastMCP("markitdown-mcp-advanced")


@mcp.tool()
async def convert_to_markdown(source: str) -> str:
    try:
        # 在线程池中执行转换（避免阻塞事件循环）
        loop = asyncio.get_event_loop()
        markdown = await loop.run_in_executor(None, convert, source)

        return markdown

    except ValueError as e:
        return f"Error: {str(e)}"
    except FileNotFoundError as e:
        return f"File not found: {str(e)}"
    except Exception as e:
        return f"Conversion failed: {str(e)}"


@mcp.tool()
async def list_supported_formats() -> str:
    """
    获取支持的文件格式列表

    Returns:
        支持的文件扩展名列表（JSON 格式）
    """
    import json
    formats = get_supported_formats()
    return json.dumps({
        "total": len(formats),
        "formats": sorted(formats)
    }, indent=2, ensure_ascii=False)


def create_starlette_app(mcp_server: Server, *, debug: bool = False) -> Starlette:
    """
    创建支持 HTTP 的 Web 应用

    Args:
        mcp_server: MCP 服务器实例
        debug: 是否开启调试模式

    Returns:
        Starlette 应用实例
    """
    session_manager = StreamableHTTPSessionManager(
        app=mcp_server,
        event_store=None,
        json_response=True,
        stateless=True,
    )

    async def handle_http(scope, receive, send):
        """处理 HTTP 请求"""
        await session_manager.handle_request(scope, receive, send)

    return Starlette(
        debug=debug,
        routes=[
            Route("/mcp", endpoint=handle_http),  # 只保留一个 HTTP 端点
        ],
    )


def main():
    """主入口函数"""
    parser = argparse.ArgumentParser(
        description="MarkItDown MCP Server with PaddleOCR support"
    )

    parser.add_argument(
        "--http",
        action="store_true",
        help="使用 HTTP 传输模式（默认：STDIO 模式）"
    )
    parser.add_argument(
        "--host",
        default="127.0.0.1",
        help="绑定主机地址（仅 HTTP 模式，默认：127.0.0.1）"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=7566,
        help="监听端口（仅 HTTP 模式，默认：7566）"
    )

    args = parser.parse_args()

    # 获取底层 MCP 服务器实例
    mcp_server = mcp._mcp_server

    if args.http:
        # HTTP 模式：启动 Web 服务器
        print(f"Starting MCP server in HTTP mode on {args.host}:{args.port}")
        starlette_app = create_starlette_app(mcp_server, debug=True)
        uvicorn.run(
            starlette_app,
            host=args.host,
            port=args.port,
        )
    else:
        # STDIO 模式：通过标准输入输出通信（默认）
        print("Starting MCP server in STDIO mode")
        mcp.run()


if __name__ == "__main__":
    main()
