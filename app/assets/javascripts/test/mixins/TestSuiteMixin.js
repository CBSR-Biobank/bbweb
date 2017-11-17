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
function TestSuiteMixin($injector) {

  return {
    injectDependencies,
    capitalizeFirstLetter,
    url
  };

  //--

  // cannot use arrow function since "arguments" is used
  function injectDependencies(...dependencies) {
    dependencies.forEach((dependency) => {
      if (dependency.trim() === '') {
        throw new Error('invalid dependency, cannot inject')
      }
      this[dependency] = $injector.get(dependency);
    });
  }

  function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  function url(...paths) {
    if (paths.length <= 0) {
      throw new Error('no arguments specified');
    }
    const allpaths = [ '/api' ].concat(paths)
    return allpaths.join('/');
 }

}

export default ngModule => ngModule.service('TestSuiteMixin', TestSuiteMixin)
