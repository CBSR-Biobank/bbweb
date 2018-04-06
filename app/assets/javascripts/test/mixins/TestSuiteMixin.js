/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * This mixin can be mixed into the `UserContext` object of a {@link https://jasmine.github.io/ Jasmine}
 * test suite.
 *
 * It provides several functions that are common in all test suites.
 *
 * @exports test.mixins.TestSuiteMixin
 */
const TestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    dependencies.forEach((dependency) => {
      if (dependency.trim() === '') {
        throw new Error('invalid dependency, cannot inject')
      }
      this[dependency] = this.$injector.get(dependency);
    });
  },

  /**
   * @param {string} string - the string to be capitalized.
   *
   * @return {string} The string passed in but with the first letter capilatized.
   */
  capitalizeFirstLetter: function (string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  },

  /**
   * Returns a URL with the given path and and correct prefix used by the Biobank Server's REST API.
   *
   * @param {...string} paths - the path elements to join with the `slash` character.
   *
   * @return {string} The URL.
   */
  url: function (...paths) {
    if (paths.length <= 0) {
      throw new Error('no arguments specified');
    }
    return [ '/api' ].concat(paths).join('/');
  }
}

export { TestSuiteMixin }
export default () => {}
