```html
<!doctype html>
<html lang="zh-CN" itemscope itemtype="http://schema.org/WebPage">
<head>
  <meta charset="utf-8">
  <title>HTML 标签覆盖验证页（HTML5+常用/少见元素示例）</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="尽可能覆盖大多数常见 HTML5 标签，便于快速验证渲染、样式与脚本行为。">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="theme-color" content="#0ea5e9">
  <meta name="author" content="ChatGPT">
  <base href="./" target="_self">
  <link rel="icon" href="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><circle cx='32' cy='32' r='30' fill='dodgerblue'/><text x='32' y='39' text-anchor='middle' font-size='28' fill='white'>H</text></svg>">
  <style>
    :root { --fg:#0b1324; --muted:#64748b; --brand:#0ea5e9; --bg:#f8fafc; }
    html,body{margin:0;padding:0;background:var(--bg);color:var(--fg);font:16px/1.6 system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, 'Noto Sans', 'Helvetica Neue', Arial}
    header,footer,nav,section,article,aside,main{padding:16px;margin:8px auto;max-width:1100px}
    header,footer{background:white; border:1px solid #e2e8f0; border-radius:12px; box-shadow:0 1px 2px rgba(0,0,0,.04)}
    h1,h2,h3{margin-top:1.4em}
    code,kbd,samp{background:#0f172a; color:#e2e8f0; padding:.2em .4em; border-radius:6px; font-family:ui-monospace, SFMono-Regular, Menlo, Consolas, monospace}
    pre{background:#0f172a;color:#e2e8f0;padding:12px;border-radius:12px;overflow:auto}
    .grid{display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:12px}
    .card{background:white; border:1px solid #e2e8f0; border-radius:12px; padding:14px}
    .muted{color:var(--muted)}
    .tag{display:inline-block;padding:.1em .5em;border:1px dashed #94a3b8;border-radius:999px;font-size:12px;color:#475569;margin:.15em .25em}
    .tbl{border-collapse:collapse;width:100%;background:white;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0}
    .tbl th, .tbl td{border:1px solid #e2e8f0;padding:8px;text-align:left}
    details{background:white;border:1px solid #e2e8f0;border-radius:12px;padding:10px}
    dialog{border:none;border-radius:16px;padding:0;box-shadow:0 10px 40px rgba(0,0,0,.15)}
    dialog > .dialog-body{padding:18px 20px;max-width:520px}
    .kbd{border:1px solid #cbd5e1;border-bottom-width:2px;border-radius:6px;padding:0 .4em;background:#f1f5f9}
    .section-title{display:flex;align-items:baseline;gap:12px}
    .section-title small{color:var(--muted)}
    .chip{display:inline-block;background:#e0f2fe;color:#0369a1;padding:.1em .6em;border-radius:999px;font-size:12px;margin-left:6px}
    .note{background:#fffbeb;border:1px solid #f59e0b;color:#92400e;padding:10px;border-radius:10px}
  </style>

  <!-- Module script + nomodule fallback -->
  <script type="module">
    // Minimal ES module script
    window.addEventListener('DOMContentLoaded', () => {
      const out = document.getElementById('module-out');
      if (out) out.textContent = '✅ ES module script 执行成功。';
      // Web Components: attachShadow + <slot> demo
      const host = document.getElementById('shadow-host');
      const tpl = document.getElementById('tpl-slot');
      if (host && tpl) {
        const shadow = host.attachShadow({mode:'open'});
        shadow.append(tpl.content.cloneNode(true));
      }
      // Dialog demo
      const btn = document.getElementById('open-dialog');
      const dlg = document.getElementById('demo-dialog');
      const close = document.getElementById('close-dialog');
      btn?.addEventListener('click', () => dlg?.showModal());
      close?.addEventListener('click', () => dlg?.close());
    });
  </script>
  <script nomodule>
    document.addEventListener('DOMContentLoaded', function(){
      var out = document.getElementById('module-out');
      if(out) out.textContent = '⚠️ 当前环境不支持 ES module，已使用 nomodule 回退脚本。';
    });
  </script>
</head>

<body>
  <noscript>
    <div class="note" role="note">此页面包含大量交互示例；若禁用 JavaScript，部分行为（如 &lt;dialog&gt;、模板渲染）将不可用。</div>
  </noscript>

  <header>
    <h1>HTML 标签覆盖验证页 <span class="chip">for QA/Validation</span></h1>
    <nav aria-label="快速导航">
      <ul class="grid" style="list-style:none;padding:0;margin:0">
        <li class="card"><a href="#semantics">语义与结构</a></li>
        <li class="card"><a href="#text-inline">文本与内联元素</a></li>
        <li class="card"><a href="#links-media">链接与媒体</a></li>
        <li class="card"><a href="#embedded">嵌入与绘图</a></li>
        <li class="card"><a href="#forms">表单元素</a></li>
        <li class="card"><a href="#interactive">交互元素</a></li>
        <li class="card"><a href="#tables">表格元素</a></li>
        <li class="card"><a href="#template-slot">模板与插槽</a></li>
        <li class="card"><a href="#misc">杂项</a></li>
      </ul>
    </nav>
    <p id="module-out" class="muted">（脚本初始化中……）</p>
  </header>

  <main>
    <section id="semantics">
      <div class="section-title">
        <h2>语义与结构（section/article/aside/main/header/footer/nav）</h2><small>包含：h1–h6, p, hr, address, blockquote, cite</small>
      </div>

      <article class="card" itemprop="mainEntity" itemscope itemtype="http://schema.org/Article">
        <header>
          <h3 itemprop="headline">示例文章标题 h3</h3>
          <p class="muted">作者 <cite itemprop="author">Alice</cite> · <time datetime="2025-10-31" itemprop="datePublished">2025-10-31</time></p>
        </header>
        <p itemprop="articleBody">这是段落 p。可以包含<em>强调 em</em>、<strong>加粗 strong</strong>、
          <mark>高亮 mark</mark>、<small>小号 small</small>、
          <s>删除线 s</s>、<u>下划线 u</u>、<abbr title="超文本标记语言">HTML</abbr>、
          <dfn title="给术语下定义">术语 dfn</dfn>、以及 <time datetime="2025-10-31T11:00">时间 time</time>。
          还可以包含 <sub>下标</sub> 与 <sup>上标</sup>，以及用于方向的 <bdi>123</bdi> 与
          <bdo dir="rtl">方向覆盖 bdo</bdo>。</p>
        <p>引用示例：<q cite="https://example.com">行内引用 q</q></p>
        <blockquote cite="https://example.com">
          <p>这是块级引用 blockquote。</p>
        </blockquote>
        <address>地址 address：陕西省西安市某某路 123 号</address>
        <hr>
        <aside class="muted">这是侧边说明 aside。</aside>
        <footer class="muted">文章页脚 footer。</footer>
      </article>

      <article class="card">
        <h4>Heading 级别示例</h4>
        <div>
          <h1>h1 大标题</h1>
          <h2>h2 副标题</h2>
          <h3>h3</h3>
          <h4>h4</h4>
          <h5>h5</h5>
          <h6>h6</h6>
        </div>
      </article>
    </section>

    <section id="text-inline">
      <div class="section-title">
        <h2>文本与内联元素</h2>
        <small>包含：span, br, wbr, code, kbd, samp, var, data, ruby, rt, rp, del, ins</small>
      </div>

      <div class="card">
        <p>普通文本 <span class="tag">&lt;span&gt;</span> 分隔，强制换行 <span class="tag">&lt;br&gt;</span>：<br>这里是新的一行。</p>
        <p>不换行长词断点 <span class="tag">&lt;wbr&gt;</span>：超超级长的标识符——thisIsAnExtremely<wbr>LongIdentifier。</p>
        <p>代码 <code>const x = 42;</code>，键位 <kbd class="kbd">Ctrl</kbd>+<kbd class="kbd">S</kbd>，样例 <samp>OK</samp>，变量 <var>x</var>。
           机器可读数据 <data value="12345">一二三四五</data>。</p>
        <p>东亚注音 <ruby>汉<rt>han</rt>字<rt>zi</rt><rp>(</rp><rt>ruby</rt><rp>)</rp></ruby> 示例。</p>
        <p>修改痕迹：<del datetime="2025-10-01">旧文本</del> <ins datetime="2025-10-02">新文本</ins></p>
        <pre><code>// 预格式化文本 pre + code
function hello(name){ console.log('Hello, ' + name); }
hello('world');</code></pre>
      </div>
    </section>

    <section id="links-media">
      <div class="section-title">
        <h2>链接与媒体</h2>
        <small>包含：a, img, picture, source, figure, figcaption, map, area</small>
      </div>

      <div class="card">
        <p><a href="#" download rel="noopener">下载链接 a[download]</a> ·
           <a href="#forms" rel="section">锚点跳转</a></p>

        <figure>
          <picture>
            <!-- 响应式图片：此处均使用内联 SVG 占位，实际项目可替换为真实资源 -->
            <source media="(min-width: 800px)" type="image/svg+xml"
              srcset="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='800' height='300'><rect width='800' height='300' fill='lightgray'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='40'>800px source</text></svg>">
            <source media="(min-width: 400px)" type="image/svg+xml"
              srcset="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='400' height='200'><rect width='400' height='200' fill='gainsboro'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='24'>400px source</text></svg>">
            <img alt="占位图片" usemap="#demo-map"
              src="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='160'><rect width='240' height='160' fill='silver'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='16'>fallback img</text></svg>">
          </picture>
          <figcaption>figure + picture + source + img + figcaption</figcaption>
        </figure>

        <!-- 图像映射 map/area -->
        <map name="demo-map">
          <area shape="rect" coords="0,0,120,80" href="#semantics" alt="语义">
          <area shape="rect" coords="120,80,240,160" href="#forms" alt="表单">
        </map>
      </div>
    </section>

    <section id="embedded">
      <div class="section-title">
        <h2>嵌入与绘图</h2>
        <small>包含：audio, video, track, canvas, svg, iframe, embed, object, param, math</small>
      </div>

      <div class="grid">
        <div class="card">
          <p>音频 <span class="tag">&lt;audio&gt;</span>（占位，无实际音源）：</p>
          <audio controls>
            <source src="" type="audio/mpeg">
            <track kind="captions" srclang="zh" label="字幕（占位）">
            你的浏览器不支持 audio 元素。
          </audio>
        </div>

        <div class="card">
          <p>视频 <span class="tag">&lt;video&gt;</span>（占位，无实际视频源）：</p>
          <video controls width="320" height="180" poster="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='320' height='180'><rect width='320' height='180' fill='lightgray'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle'>poster</text></svg>">
            <source src="" type="video/mp4">
            <track kind="subtitles" srclang="en" label="Subtitles (placeholder)">
            你的浏览器不支持 video 元素。
          </video>
        </div>

        <div class="card">
          <p>Canvas 绘图：</p>
          <canvas id="cv" width="220" height="120" style="border:1px solid #e2e8f0;border-radius:8px"></canvas>
          <script>
            (function(){
              const c = document.getElementById('cv');
              if (!c) return;
              const g = c.getContext('2d');
              g.fillStyle = '#e2e8f0'; g.fillRect(0,0,c.width,c.height);
              g.fillStyle = '#0ea5e9'; g.fillRect(10,10,80,80);
              g.fillStyle = '#0b1324'; g.font = '14px sans-serif';
              g.fillText('canvas demo', 100, 60);
            })();
          </script>
        </div>

        <div class="card">
          <p>SVG 内联：</p>
          <svg viewBox="0 0 120 80" width="240" height="160" role="img" aria-label="Simple SVG">
            <rect x="1" y="1" width="118" height="78" fill="#e2e8f0" stroke="#94a3b8"/>
            <circle cx="40" cy="40" r="18" fill="#0ea5e9"/>
            <text x="65" y="45" font-size="10">SVG</text>
          </svg>
        </div>

        <div class="card">
          <p>iframe（srcdoc）：</p>
          <iframe title="iframe demo" width="100%" height="120" loading="lazy"
            srcdoc="<!doctype html><html><body style='margin:0;font:14px sans-serif'><p style='padding:10px'>Hello from <b>iframe</b> srcdoc.</p></body></html>"></iframe>
        </div>

        <div class="card">
          <p>embed / object（均用内联 SVG 占位）：</p>
          <embed type="image/svg+xml" width="120" height="80"
            src="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='80'><rect width='120' height='80' fill='lavender'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='12'>embed</text></svg>">
          <object type="image/svg+xml" width="120" height="80"
            data="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='80'><rect width='120' height='80' fill='honeydew'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='12'>object</text></svg>">
            <param name="example" value="param-value">
          </object>
        </div>

        <div class="card">
          <p>MathML（简例，浏览器支持度各异）：</p>
          <math display="block">
            <mrow>
              <mi>e</mi><mo>^</mo><mi>i</mi><mi>&#x03C0;</mi><mo>+</mo><mn>1</mn><mo>=</mo><mn>0</mn>
            </mrow>
          </math>
        </div>
      </div>
    </section>

    <section id="forms">
      <div class="section-title">
        <h2>表单元素</h2>
        <small>包含：form, label, input（多类型）, textarea, select, optgroup, option, datalist, fieldset, legend, button, output, meter, progress</small>
      </div>

      <form class="card" oninput="sum.value = (parseFloat(a.value)||0) + (parseFloat(b.value)||0)">
        <fieldset>
          <legend>基本输入</legend>
          <p><label>文本 <input type="text" name="text" placeholder="text"></label></p>
          <p><label>密码 <input type="password" name="pwd" value="secret"></label></p>
          <p><label>邮箱 <input type="email" name="email" placeholder="you@example.com"></label></p>
          <p><label>URL <input type="url" name="url" placeholder="https://"></label></p>
          <p><label>电话 <input type="tel" name="tel" placeholder="13800000000"></label></p>
          <p><label>搜索 <input type="search" name="q" placeholder="关键字"></label></p>
          <p><label>数字 <input type="number" name="num" min="0" max="10" step="1" value="3"></label></p>
          <p><label>范围 <input type="range" name="rng" min="0" max="100" value="40"></label></p>
          <p><label>颜色 <input type="color" name="clr" value="#0ea5e9"></label></p>
          <p><label>日期 <input type="date" name="date"></label></p>
          <p><label>时间 <input type="time" name="time"></label></p>
          <p><label>日期时间 <input type="datetime-local" name="dt"></label></p>
          <p><label>月份 <input type="month" name="month"></label></p>
          <p><label>星期 <input type="week" name="week"></label></p>
          <p><label>文件 <input type="file" name="file" multiple></label></p>
          <p><label>隐藏 <input type="hidden" name="hid" value="hidden-value"></label></p>
        </fieldset>

        <fieldset>
          <legend>选择控件</legend>
          <p>
            <label><input type="checkbox" name="ck1" checked> 复选 1</label>
            <label><input type="checkbox" name="ck2"> 复选 2</label>
          </p>
          <p>
            <label><input type="radio" name="r1" value="A" checked> 单选 A</label>
            <label><input type="radio" name="r1" value="B"> 单选 B</label>
          </p>
          <p>
            <label for="sel">下拉选择</label>
            <select id="sel" name="sel">
              <optgroup label="分组 1">
                <option>选项 1</option>
                <option>选项 2</option>
              </optgroup>
              <optgroup label="分组 2">
                <option selected>选项 3</option>
              </optgroup>
            </select>
          </p>
          <p>
            <label for="dl">自动完成 datalist</label>
            <input list="dl" name="dl-input" placeholder="输入字母试试">
            <datalist id="dl">
              <option value="Apple">
              <option value="Banana">
              <option value="Cherry">
            </datalist>
          </p>
        </fieldset>

        <fieldset>
          <legend>长文本与结果</legend>
          <p><label>多行文本 <textarea name="ta" rows="3" cols="30">多行文本…</textarea></label></p>
          <p>进度 progress：<progress value="30" max="100"></progress></p>
          <p>计量 meter：<meter min="0" low="30" high="80" max="100" optimum="70" value="65">65</meter></p>
          <p>计算 output：<input name="a" type="number" value="1"> + <input name="b" type="number" value="2"> = <output name="sum" for="a b">3</output></p>
        </fieldset>

        <p>
          <button type="submit">提交</button>
          <button type="reset">重置</button>
          <button type="button" onclick="alert('按钮被点击')">普通按钮</button>
        </p>
      </form>
    </section>

    <section id="interactive">
      <div class="section-title">
        <h2>交互元素</h2>
        <small>包含：details/summary, dialog</small>
      </div>

      <details class="card">
        <summary>点我展开（details/summary）</summary>
        <p>这里是 details 展开内容。</p>
      </details>

      <div class="card">
        <button id="open-dialog">打开 &lt;dialog&gt;</button>
        <dialog id="demo-dialog">
          <div class="dialog-body">
            <h3>对话框 dialog</h3>
            <p>这是一个原生 dialog。点击外侧或按钮关闭。</p>
            <form method="dialog">
              <button id="close-dialog">关闭</button>
            </form>
          </div>
        </dialog>
      </div>
    </section>

    <section id="tables">
      <div class="section-title">
        <h2>表格元素</h2>
        <small>包含：table, caption, colgroup, col, thead, tbody, tfoot, tr, th, td</small>
      </div>

      <table class="tbl">
        <caption>表格标题 caption</caption>
        <colgroup>
          <col span="1" style="background:#f8fafc">
          <col span="1" style="background:#f1f5f9">
          <col span="1" style="background:#eef2ff">
        </colgroup>
        <thead>
          <tr><th>列 A</th><th>列 B</th><th>列 C</th></tr>
        </thead>
        <tbody>
          <tr><td>A1</td><td>B1</td><td>C1</td></tr>
          <tr><td>A2</td><td>B2</td><td>C2</td></tr>
        </tbody>
        <tfoot>
          <tr><td colspan="3">表尾 tfoot</td></tr>
        </tfoot>
      </table>
    </section>

    <section id="template-slot">
      <div class="section-title">
        <h2>模板与插槽</h2>
        <small>包含：template, slot（通过 Shadow DOM 演示）</small>
      </div>

      <template id="tpl-slot">
        <style>:host{display:block}</style>
        <div class="card" style="margin:0">
          来自 <code>&lt;template&gt;</code> 的内容，插槽：<slot name="s1">（默认插槽内容）</slot>
        </div>
      </template>

      <div id="shadow-host" class="card">
        <span slot="s1">这是分发到命名插槽 <code>name="s1"</code> 的内容。</span>
        <p class="muted" style="margin-top:8px">此容器成为 Shadow Host，脚本会将模板克隆到其 ShadowRoot。</p>
      </div>
    </section>

    <section id="misc">
      <div class="section-title">
        <h2>杂项</h2>
        <small>包含：main, figure/figcaption（已示），script/style/link/base/meta/noscript</small>
      </div>

      <div class="card">
        <p>本页已包含：<span class="tag">doctype</span><span class="tag">html</span><span class="tag">head</span><span class="tag">body</span>
          <span class="tag">meta</span><span class="tag">title</span><span class="tag">link</span><span class="tag">base</span>
          <span class="tag">style</span><span class="tag">script (module/nomodule)</span><span class="tag">noscript</span> 等。</p>
        <p class="muted">注：为保持标准有效性与现代实践，未包含过时/废弃元素（如 <code>center</code>、<code>font</code>、<code>marquee</code> 等）。若你需要用于“兼容性/解析器”验证，我可以单独输出一份“含过时标签版”。</p>
      </div>
    </section>
  </main>

  <footer>
    <p>页脚 footer · 本页用于标签验证与教学演示 · <time datetime="2025-10-31">2025-10-31</time></p>
  </footer>
</body>
</html>

```