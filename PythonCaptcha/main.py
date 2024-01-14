from flask import Flask, request, send_file, jsonify
from PIL import Image, ImageDraw, ImageFont
import random
import base64
import string
import io

app = Flask(__name__)

def encode_image_to_base64(image_path):
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    return encoded_string

def decode_base64_to_image(encoded_string):
    return Image.open(io.BytesIO(base64.b64decode(encoded_string)))


def drawLines(draw, height, width):
    line_count = random.randint(10, 15)
    for i in range(line_count):
        x1 = random.randint(0, width)
        y1 = random.randint(0, height)
        x2 = random.randint(0, width)
        y2 = random.randint(0, height)
        draw.line((x1, y1, x2, y2), fill=(0, 0, 0))

def drawDots(draw, height, width):
    dot_count = random.randint(500, 1000)
    for i in range(dot_count):
        x = random.randint(0, width)
        y = random.randint(0, height)
        draw.point((x, y), fill=(0, 0, 0))

@app.route('/captcha/<captcha_code>', methods=['GET'])
def generate_captcha(captcha_code: str):
    width = 30 * len(captcha_code)
    height = 50

    image = Image.new('RGB', (width, height), (255, 255, 255))
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('font2.ttf', 36)

    code_chars = list(captcha_code)
    for i, c in enumerate(code_chars):
        x = 10 + i * 30 + random.randint(-5, 5)
        y = 10 + random.randint(-5, 5)
        draw.text((x, y),
                  c,
                  font=font,
                  fill=(random.randint(50, 175), random.randint(50, 175),
                        random.randint(50, 175)))

    drawLines(draw, height, width)

    drawDots(draw, height, width)

    # Save the image to a BytesIO object
    img_byte_array = io.BytesIO()
    image.save(img_byte_array, format='PNG')

    # Seek to the beginning of the BytesIO object
    img_byte_array.seek(0)

    # Encode the image to base64
    return base64.b64encode(img_byte_array.read()).decode('utf-8')

    #return send_file(img_byte_array, mimetype='image/png')

if __name__ == '__main__':
    app.run(host='localhost', port=1111)
