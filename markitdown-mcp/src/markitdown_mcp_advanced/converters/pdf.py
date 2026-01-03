from pathlib import Path
# tested

def convert(source: Path) -> str:
    # Paddle API调用
    import base64
    from markitdown_mcp_advanced.config import Config
    import requests

    API_URL = Config.PADDLE_API_URL
    TOKEN = Config.PADDLE_TOKEN

    file_path = source

    with open(file_path, "rb") as file:
        file_bytes = file.read()
        file_data = base64.b64encode(file_bytes).decode("ascii")

    headers = {
        "Authorization": f"token {TOKEN}",
        "Content-Type": "application/json"
    }

    required_payload = {
                           "file": file_data,
                           "fileType": 0,  # For PDF documents, set `fileType` to 0; for images, set `fileType` to 1
    }

    optional_payload = {
        "useDocOrientationClassify": False,
        "useDocUnwarping": False,
        "useTextlineOrientation": False,
        "useChartRecognition": False,
    }

    payload = {**required_payload, **optional_payload}

    response = requests.post(API_URL, json=payload, headers=headers)

    #
    assert response.status_code == 200
    result = response.json()["result"]

    # output_dir = r"O:\Project\markitdown\temp"
    # os.makedirs(output_dir, exist_ok=True)
    markdown = ""
    for i, res in enumerate(result["layoutParsingResults"]):
        markdown += res["markdown"]["text"]
        # md_filename = os.path.join(output_dir, f"doc_{i}.md")
        # with open(md_filename, "w", encoding="utf-8") as md_file:
        #     md_file.write(res["markdown"]["text"])
        # print(f"Markdown document saved at {md_filename}")
        # for img_path, img in res["markdown"]["images"].items():
        #     full_img_path = os.path.join(output_dir, img_path)
        #     os.makedirs(os.path.dirname(full_img_path), exist_ok=True)
        #     img_bytes = requests.get(img).content
        #     with open(full_img_path, "wb") as img_file:
        #         img_file.write(img_bytes)
        #     print(f"Image saved to: {full_img_path}")
        # for img_name, img in res["outputImages"].items():
        #     img_response = requests.get(img)
        #     if img_response.status_code == 200:
        #         # Save image to local
        #         filename = os.path.join(output_dir, f"{img_name}_{i}.jpg")
        #         with open(filename, "wb") as f:
        #             f.write(img_response.content)
        #         print(f"Image saved to: {filename}")
        #     else:
        #         print(f"Failed to download image, status code: {img_response.status_code}")
    # 调用 OCR 逻辑
    return markdown



