<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>BudgetBuddy API Docs</title>
    <meta name="robots" content="noindex" />
    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui.css" />
    <style>
        /* ── Base ──────────────────────────────────────────────────────── */
        body  { margin: 0; background: #0D0D0D; font-size: 16px; }

        /* ── Topbar ────────────────────────────────────────────────────── */
        .swagger-ui .topbar { background: #111; border-bottom: 1px solid rgba(110,220,211,0.2); padding: 10px 0; }
        .swagger-ui .topbar .download-url-wrapper { display: none; }
        .swagger-ui .topbar-wrapper a span { display: none; }
        .swagger-ui .topbar-wrapper a::after {
            content: 'BudgetBuddy API';
            color: #6EDCD3;
            font-size: 1.25rem;
            font-weight: 700;
            letter-spacing: -0.01em;
        }
        .swagger-ui .topbar-wrapper img { display: none; }

        /* ── Main wrapper ──────────────────────────────────────────────── */
        .swagger-ui { background: #0D0D0D; }
        .swagger-ui .wrapper { max-width: 1100px; padding: 0 24px; }

        /* ── Info block ────────────────────────────────────────────────── */
        .swagger-ui .info { margin: 32px 0 24px; }
        .swagger-ui .info .title { color: #fff; font-size: 2rem; font-weight: 700; }
        .swagger-ui .info .title small { background: #6EDCD3; color: #0D0D0D; font-size: 0.75rem; font-weight: 700; padding: 2px 8px; border-radius: 6px; }
        .swagger-ui .info p,
        .swagger-ui .info li,
        .swagger-ui .info table { color: #bbb; font-size: 0.9375rem; line-height: 1.7; }
        .swagger-ui .info a { color: #6EDCD3; }
        .swagger-ui .info code { background: #1A1A1A; color: #6EDCD3; padding: 2px 6px; border-radius: 4px; font-size: 0.875rem; }
        .swagger-ui .info pre { background: #1A1A1A; border: 1px solid rgba(255,255,255,0.08); border-radius: 10px; padding: 14px 16px; }
        .swagger-ui .info pre code { background: none; padding: 0; }
        .swagger-ui .info h2 { color: #e0e0e0; font-size: 1.125rem; margin-top: 1.5rem; }
        .swagger-ui .info table th { color: #6EDCD3; font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 6px; }
        .swagger-ui .info table td { color: #bbb; font-size: 0.9rem; padding: 6px 12px 6px 0; }

        /* ── Servers bar ───────────────────────────────────────────────── */
        .swagger-ui .scheme-container { background: #111; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 14px 20px; margin: 0 0 24px; box-shadow: none; }
        .swagger-ui .schemes > label { color: #888; font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.05em; }
        .swagger-ui select { background: #1A1A1A; border: 1px solid rgba(255,255,255,0.12); color: #e0e0e0; border-radius: 8px; padding: 6px 10px; font-size: 0.9rem; }
        .swagger-ui .btn.authorize { background: #6EDCD3; color: #0D0D0D; border: none; border-radius: 8px; font-weight: 600; font-size: 0.875rem; padding: 8px 18px; }
        .swagger-ui .btn.authorize svg { fill: #0D0D0D; }

        /* ── Tag groups ────────────────────────────────────────────────── */
        .swagger-ui .opblock-tag { color: #fff; font-size: 1.125rem; font-weight: 600; border-bottom: 1px solid rgba(255,255,255,0.07); padding-bottom: 8px; margin-bottom: 4px; }
        .swagger-ui .opblock-tag:hover { background: transparent; }
        .swagger-ui .opblock-tag small { color: #888; font-size: 0.875rem; font-weight: 400; }

        /* ── Operation blocks ──────────────────────────────────────────── */
        .swagger-ui .opblock { border-radius: 10px; border: 1px solid rgba(255,255,255,0.08) !important; margin-bottom: 10px; background: #1A1A1A !important; box-shadow: none !important; }
        .swagger-ui .opblock .opblock-summary { padding: 12px 16px; border-radius: 10px; }
        .swagger-ui .opblock .opblock-summary-method { border-radius: 6px; font-size: 0.8125rem; font-weight: 700; min-width: 72px; text-align: center; }
        .swagger-ui .opblock .opblock-summary-path { color: #e0e0e0 !important; font-size: 0.9375rem; font-weight: 500; }
        .swagger-ui .opblock .opblock-summary-description { color: #888; font-size: 0.875rem; }
        .swagger-ui .opblock.opblock-get    { border-left: 3px solid #61affe !important; }
        .swagger-ui .opblock.opblock-post   { border-left: 3px solid #49cc90 !important; }
        .swagger-ui .opblock.opblock-put    { border-left: 3px solid #fca130 !important; }
        .swagger-ui .opblock.opblock-delete { border-left: 3px solid #f93e3e !important; }

        /* ── Expanded operation body ───────────────────────────────────── */
        .swagger-ui .opblock-body { background: #141414; border-top: 1px solid rgba(255,255,255,0.06); padding: 16px; border-radius: 0 0 10px 10px; }
        .swagger-ui .opblock-description-wrapper p,
        .swagger-ui .opblock-external-docs-wrapper p,
        .swagger-ui .tab-header .tab-item p,
        .swagger-ui table.model tbody tr td,
        .swagger-ui .response-col_description,
        .swagger-ui .parameters-col_description { color: #ccc; font-size: 0.9rem; line-height: 1.6; }
        .swagger-ui .opblock-section-header h4 { color: #e0e0e0; font-size: 0.875rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
        .swagger-ui .opblock-section-header { background: #1e1e1e; border-bottom: 1px solid rgba(255,255,255,0.06); border-radius: 6px 6px 0 0; padding: 10px 16px; }

        /* ── Parameters table ──────────────────────────────────────────── */
        .swagger-ui table thead tr th { color: #6EDCD3; font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid rgba(255,255,255,0.08); }
        .swagger-ui table tbody tr td { color: #ccc; font-size: 0.9rem; border-bottom: 1px solid rgba(255,255,255,0.04); padding: 10px 0; }
        .swagger-ui .parameter__name { color: #e0e0e0; font-weight: 600; }
        .swagger-ui .parameter__type { color: #6EDCD3; font-size: 0.8rem; }
        .swagger-ui .parameter__deprecated { color: #f93e3e; }
        .swagger-ui .required { color: #f93e3e !important; }

        /* ── Try-it-out inputs ─────────────────────────────────────────── */
        .swagger-ui input[type=text],
        .swagger-ui input[type=email],
        .swagger-ui input[type=file],
        .swagger-ui textarea { background: #252525 !important; border: 1px solid rgba(255,255,255,0.12) !important; color: #e0e0e0 !important; border-radius: 8px !important; padding: 8px 12px !important; font-size: 0.9rem !important; }
        .swagger-ui .btn { border-radius: 8px; font-size: 0.875rem; font-weight: 600; }
        .swagger-ui .btn.execute { background: #6EDCD3; color: #0D0D0D; border: none; }
        .swagger-ui .btn.cancel  { background: transparent; color: #888; border: 1px solid rgba(255,255,255,0.15); }
        .swagger-ui .try-out__btn { background: transparent; border: 1px solid #6EDCD3; color: #6EDCD3; border-radius: 8px; }

        /* ── Response section ──────────────────────────────────────────── */
        .swagger-ui .responses-inner { background: #141414; }
        .swagger-ui .response-col_status { color: #e0e0e0; font-weight: 600; font-size: 0.9rem; }
        .swagger-ui .response-col_links  { color: #888; }
        .swagger-ui .microlight { background: #111 !important; color: #6EDCD3 !important; border-radius: 8px; padding: 12px 16px; font-size: 0.875rem !important; line-height: 1.6; }
        .swagger-ui .highlight-code { background: #111 !important; }
        .swagger-ui .renderedMarkdown p { color: #bbb; font-size: 0.9rem; }
        .swagger-ui .model-box { background: #1A1A1A; border: 1px solid rgba(255,255,255,0.07); border-radius: 8px; }
        .swagger-ui section.models { background: #111; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 0; }
        .swagger-ui section.models h4 { color: #e0e0e0; font-size: 1rem; }
        .swagger-ui .model-title { color: #6EDCD3; }
        .swagger-ui .model { color: #ccc; font-size: 0.875rem; }

        /* ── No-ops filter ─────────────────────────────────────────────── */
        .swagger-ui .filter-container { background: #111; border-bottom: 1px solid rgba(255,255,255,0.06); }
        .swagger-ui .filter-container input { background: #1A1A1A !important; border: 1px solid rgba(255,255,255,0.1) !important; color: #e0e0e0 !important; border-radius: 8px !important; }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>

    <script src="https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-standalone-preset.js"></script>
    <script>
        SwaggerUIBundle({
            url: "{{ asset('api-docs/openapi.yaml') }}",
            dom_id: '#swagger-ui',
            presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
            layout: 'StandaloneLayout',
            deepLinking: true,
            displayRequestDuration: true,
            filter: true,
            tryItOutEnabled: true,
            defaultModelsExpandDepth: 1,
            defaultModelExpandDepth: 2,
        });
    </script>
</body>
</html>
