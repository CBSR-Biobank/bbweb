/**
 * A mixin for test suites.
 *
 * @mixin testSuiteMixin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
/* @ngInject */
function TestSuiteMixin($injector, UrlService) {

  return {
    injectDependencies,
    capitalizeFirstLetter,
    url
  };

  //--

  // cannot use arrow function since "arguments" is used
  function injectDependencies(...dependencies) {
    dependencies.forEach((dependency) => {
      this[dependency] = $injector.get(dependency);
    });
  }

  function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  function url(...paths) {
    return UrlService.url.apply(UrlService, paths);
  }

}

export default ngModule => ngModule.service('TestSuiteMixin', TestSuiteMixin)
