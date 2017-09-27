/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

export default class UrlService {

  constructor(AppConfig) {
    this.AppConfig = AppConfig;
  }

  /**
   * Returns a URL with the given path.
   *
   * @return {string} The URL.
   */
  url(/* baseUrl, pathItem1, pathItem2, ... pathItemN */) {
    const args = [ this.AppConfig.restApiUrlPrefix ].concat(_.toArray(arguments));
    if (args.length <= 0) {
      throw new Error('no arguments specified');
    }
    return args.join('/');
  }
}

UrlService.$inject = ['AppConfig'];
