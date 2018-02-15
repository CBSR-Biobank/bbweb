/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

class UrlService {

  constructor(AppConfig) {
    'ngInject'
    Object.assign(this, { AppConfig })
  }

  /**
   * Returns a URL with the given path.
   *
   * @return {string} The URL.
   */
  url(...paths) {
    const args = [ this.AppConfig.restApiUrlPrefix ].concat(paths)
    if (args.length <= 0) {
      throw new Error('no arguments specified')
    }
    return args.join('/')
  }
}

export default ngModule => ngModule.service('UrlService', UrlService)
