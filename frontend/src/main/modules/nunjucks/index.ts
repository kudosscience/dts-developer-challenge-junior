import * as path from 'path';

import * as express from 'express';
import * as nunjucks from 'nunjucks';

export class Nunjucks {
  constructor(public developmentMode: boolean) {
    this.developmentMode = developmentMode;
  }

  enableFor(app: express.Express): void {
    app.set('view engine', 'njk');
    const viewsDir = path.join(__dirname, '..', '..', 'views');
    const govukDir = path.join(__dirname, '..', '..', '..', '..', 'node_modules', 'govuk-frontend');
    
    nunjucks.configure([viewsDir, govukDir], {
      autoescape: true,
      watch: this.developmentMode,
      express: app,
    });

    app.use((req, res, next) => {
      res.locals.pagePath = req.path;
      next();
    });
  }
}
