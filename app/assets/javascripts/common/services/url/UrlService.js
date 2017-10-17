/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

export default class UrlService {

  constructor(AppConfig) {
    this.AppConfig = AppConfig;
  }

  /**
   * Returns a URL with the given path.
   *
   * @return {string} The URL.
   */
  url(...paths) {
    const args = [ this.AppConfig.restApiUrlPrefix ].concat(paths);
    if (args.length <= 0) {
      throw new Error('no arguments specified');
    }
    return args.join('/');
  }
}

UrlService.$inject = ['AppConfig'];
