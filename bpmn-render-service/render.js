const puppeteer = require('puppeteer');
const {renderSvgToPdf} = require('./renderSvgToPdf');

async function renderBpmn(xml, format = 'png') {
    console.log('[render] Starting Puppeteer...');
    const browser = await puppeteer.launch({
        headless: 'new',
        executablePath: '/usr/bin/chromium',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();
    await page.setViewport({width: 1920, height: 1080});

    // Подключаем лог консоли браузера
    page.on('console', msg => {
        msg.args().forEach(async arg => {
            console.log('[page]', await arg.jsonValue());
        });
    });

    console.log('[render] Setting page content...');
    await page.setContent(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <style>
        html, body, #canvas {
          margin: 0; padding: 0;
          width: 100vw;
          height: 100vh;
          overflow: hidden;
        }
        svg {
          width: 100%;
          height: 100%;
        }
      </style>
    </head>
    <body>
      <div id="canvas"></div>
      <script src="https://unpkg.com/bpmn-js@11.5.0/dist/bpmn-viewer.development.js"></script>
      <script>
        const viewer = new BpmnJS({ container: '#canvas' });
        viewer.importXML(\`${xml.replace(/`/g, '\\`')}\`).then(() => {
          viewer.get('canvas').zoom('fit-viewport');

          // SVG экспорт
          viewer.saveSVG({ format: true }, function(err, svg) {
            if (err) {
              console.log('Failed to export SVG');
              window._bpmnReady = false;
            } else {
              console.log('SVG export success');
              window._exportedSvg = svg;
              window._bpmnReady = true;
            }
          });
        });
      </script>
    </body>
    </html>
  `, {waitUntil: 'networkidle0'});

    await page.waitForFunction('window._bpmnReady === true', {timeout: 30000});
    console.log(`[render] Format requested: ${format}`);

    let result;

    if (format === 'svg') {
        const svg = await page.evaluate(() => window._exportedSvg);
        result = Buffer.from(svg);
        console.log('[render] SVG captured successfully via saveSVG().');
    } else if (['png', 'jpeg'].includes(format)) {
        const element = await page.$('#canvas');
        result = await element.screenshot({type: format});
        console.log('[render] Screenshot taken successfully.');
    } else if (format === 'pdf') {
        const svg = await page.evaluate(() => window._exportedSvg);
        result = await renderSvgToPdf(svg, {
            format: 'A4',
            landscape: true,
            margin: 'normal'
        });
        console.log('[render] PDF rendered from SVG with layout fitting.');
    } else {
        await browser.close();
        throw new Error(`Unsupported format: ${format}`);
    }

    await browser.close();
    console.log('[render] Done!');
    return result;
}

module.exports = {renderBpmn};
