const express = require('express');
const multer = require('multer');
const {renderBpmn} = require('./render');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');

const app = express();
const upload = multer();

// Swagger UI
const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

// BPMN Render Endpoint
app.post('/render', upload.single('file'), async (req, res) => {
    const format = req.query.format || 'png';
    console.log(`[api] POST /render called, format=${format}`);

    if (!req.file?.buffer) {
        console.warn('[api] No file uploaded');
        return res.status(400).send('No file uploaded');
    }

    try {
        const result = await renderBpmn(req.file.buffer.toString(), format);
        console.log('[api] Rendering succeeded');

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
        console.error('[api] Rendering failed:', err.message);
        res.status(500).send('Rendering failed');
    }
});

// Start
app.listen(3000, () => {
    console.log('BPMN renderer listening on port 3000');
});
