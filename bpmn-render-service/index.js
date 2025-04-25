// index.js
const express = require('express');
const multer = require('multer');
const {renderBpmn} = require('./render');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');

const app = express();
const upload = multer();

// чтобы парсить «голый» XML в теле
app.use(express.text({type: ['application/xml', 'text/xml', 'text/plain']}));

// Swagger UI
const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

// Общая функция-обработчик
async function handleRender(xml, format, res) {
    try {
        const result = await renderBpmn(xml, format);
        let contentType;
        switch (format) {
            case 'svg':
                contentType = 'image/svg+xml';
                break;
            case 'pdf':
                contentType = 'application/pdf';
                break;
            default:
                contentType = `image/${format}`;
        }
        res
            .set('Content-Disposition', `attachment; filename=diagram.${format}`)
            .contentType(contentType)
            .send(result);
    } catch (err) {
        console.error('[api] Rendering failed:', err);
        res.status(500).send('Rendering failed');
    }
}

// 1) Существующий маршрут: читает файл multipart/form-data
app.post('/render/bpmn-file', upload.single('file'), async (req, res) => {
    app.use((req, res, next) => {
        console.log('[req]', req.method, req.path);
        next();
    });
    const format = req.query.format || 'png';
    if (!req.file?.buffer) {
        return res.status(400).send('No file uploaded');
    }
    await handleRender(req.file.buffer.toString(), format, res);
});

// 2) Новый маршрут: принимает «голый» XML в теле запроса
app.post('/render/bpmn-xml', async (req, res) => {
    app.use((req, res, next) => {
        console.log('[req]', req.method, req.path);
        next();
    });
    const format = req.query.format || 'png';
    const xml = req.body;
    if (!xml || !xml.trim()) {
        return res.status(400).send('No BPMN XML provided in request body');
    }
    await handleRender(xml, format, res);
});

// Start
app.listen(3000, () => {
    console.log('BPMN renderer listening on port 3000');
});
