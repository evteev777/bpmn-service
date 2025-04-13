const puppeteer = require('puppeteer');

async function renderSvgToPdf(svgContent, options = {}) {
    const {
        format = 'A4',
        landscape = true,
        margin = 'normal' // 'none' | 'narrow' | 'normal' | 'wide'
    } = options;

    const marginPresets = {
        none: {top: '0mm', right: '0mm', bottom: '0mm', left: '0mm'},
        narrow: {top: '5mm', right: '5mm', bottom: '5mm', left: '5mm'},
        normal: {top: '10mm', right: '10mm', bottom: '10mm', left: '10mm'},
        wide: {top: '20mm', right: '20mm', bottom: '20mm', left: '20mm'}
    };

    const pdfMargin = marginPresets[margin] || marginPresets.normal;

    const browser = await puppeteer.launch({
        headless: 'new',
        executablePath: '/usr/bin/chromium',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    // Вставляем SVG как часть адаптивного layout
    await page.setContent(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <style>
        html, body {
          margin: 0;
          padding: 0;
          width: 100%;
          height: 100%;
        }
        #wrapper {
          width: 100%;
          height: 100%;
          display: flex;
          justify-content: center;
          align-items: center;
        }
        svg {
          max-width: 100%;
          max-height: 100%;
          height: auto;
          width: auto;
        }
      </style>
    </head>
    <body>
      <div id="wrapper">
        ${svgContent}
      </div>
    </body>
    </html>
  `);

    const pdfBuffer = await page.pdf({
        format,
        landscape,
        printBackground: true,
        margin: pdfMargin
    });

    await browser.close();
    return pdfBuffer;
}

module.exports = {renderSvgToPdf};
