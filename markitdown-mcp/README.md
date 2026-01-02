# MarkItDown MCP Advanced

A powerful MCP (Model Context Protocol) server that converts various document formats to Markdown with PaddleOCR support.

## Installation

Install from PyPI using uv:

```bash
uv pip install markitdown-mcp-advanced
```

Or with pip:

```bash
pip install markitdown-mcp-advanced
```

## Features

- **Multi-format Support**: PDF, images, Office documents, HTML, CSV
- **OCR Integration**: High-accuracy text recognition via PaddleOCR API
- **URL Support**: Direct processing of remote file URLs
- **MCP Protocol**: Full compliance with MCP standard (STDIO and HTTP modes)
- **Lightweight**: Core features use only Python standard library

## Supported Formats

| Category | Extensions |
|----------|------------|
| PDF | `.pdf` |
| Images | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff`, `.webp` |
| Word | `.docx` |
| PowerPoint | `.pptx` |
| Excel | `.xlsx`, `.xls` (requires extra dependency) |
| Web | `.html`, `.htm` |
| CSV | `.csv` |

## Configuration

### PaddleOCR API Credentials (Required)

Get your API URL and token from [PaddleOCR AI Studio](https://aistudio.baidu.com/paddleocr/) - Click the "API" button to obtain your credentials.

```bash
export PADDLE_API_URL="your_paddle_api_url"
export PADDLE_TOKEN="your_paddle_token"
```

### Optional Configuration

```bash
export MARKITDOWN_TEMP_DIR="your_temp_dir"  # Default: system temp directory
```


## Usage with Claude Desktop

Add to Claude Desktop config file:


```json
{
  "mcpServers": {
    "markitdown": {
      "command": "uvx",
      "args": ["--from","markitdown-mcp-advanced", "markitdown-mcp"],
      "env": {
        "PADDLE_API_URL": "your_api_url",
        "PADDLE_TOKEN": "your_token",
        "MARKITDOWN_TEMP_DIR": "your_temp_dir"
      }
    }
  }
}
```

Restart Claude Desktop after updating the configuration.

### Usage Examples

Once configured, you can use the tool in Claude Desktop:

```
Convert this PDF to Markdown: /path/to/document.pdf
```

```
Download and convert this webpage: https://example.com/article.html
```

```
Convert this Excel file: /path/to/data.xlsx
```

## MCP Tools

### `convert_to_markdown`

Converts supported file formats or URLs to Markdown text.

**Parameters:**
- `source` (string): File path or URL to convert

**Example usage in Claude Desktop:**
```
Convert this file: /path/to/document.docx
```

### `list_supported_formats`

Returns a list of all supported file formats.

**Example usage in Claude Desktop:**
```
Show me all supported file formats
```

## Links

- [GitHub Repository](https://github.com/DuanYan007/markitdown)
- [Issue Tracker](https://github.com/DuanYan007/markitdown/issues)

## License

MIT License
