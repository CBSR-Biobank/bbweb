/**
 * A mixin for test suites.
 *
 * @mixin testSuiteMixin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
/* @ngInject */
export default function TestSuiteMixin($injector, UrlService) {

  return {
    injectDependencies:    injectDependencies,
    capitalizeFirstLetter: capitalizeFirstLetter,
    url:                   url
  };

  //--

  // cannot use arrow function since "arguments" is used
  function injectDependencies(/* dep1, dep2, ..., depn */) {
    Array.from(arguments).forEach((dependency) => {
      this[dependency] = $injector.get(dependency);
    });
  }

  function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  function url() {
    return UrlService.url.apply(UrlService, _.toArray(arguments));
  }

}
