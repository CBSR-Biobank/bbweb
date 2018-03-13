/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function TestSuiteMixinService($injector) {

  /**
   * This mixin that can be added to the UserContext object of a {@link https://jasmine.github.io/ Jasmine}
   * test suite.
   *
   * It provides several functions that are common in all test suites.
   *
   * @mixin test.mixins.TestSuiteMixin
   */
  const TestSuiteMixin = {
    injectDependencies,
    capitalizeFirstLetter,
    url
  };

  /**
   * Used to inject AngularJS dependencies into the Jasmine test suite.
   */
  function injectDependencies(...dependencies) {
    dependencies.forEach((dependency) => {
      if (dependency.trim() === '') {
        throw new Error('invalid dependency, cannot inject')
      }
      this[dependency] = $injector.get(dependency);
    });
  }

  /**
   * @param {string} string - the string to be capitalized.
   *
   * @return {string} The string passed in but with the first letter capilatized.
   */
  function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  /**
   * Returns a URL with the given path and and correct prefix used by the Biobank Server's REST API.
   *
   * @return {string} The URL.
   */
  function url(...paths) {
    if (paths.length <= 0) {
      throw new Error('no arguments specified');
    }
    return [ '/api' ].concat(paths).join('/');
  }

  return TestSuiteMixin;

}

export default ngModule => ngModule.service('TestSuiteMixin', TestSuiteMixinService)
