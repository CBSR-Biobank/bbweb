/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import faker  from 'faker';

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
   * @param {...string} dependencies - the AngularJS dependencies to inject. Duplicates in this array are
   * allowed, but the dependency will only be injected once.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    dependencies.forEach((dependency) => {
      if (dependency.trim() === '') {
        throw new Error('invalid dependency, cannot inject')
      }

      // omit duplicates
      if (!this[dependency]) {
        this[dependency] = this.$injector.get(dependency);
      }
    });
  },

  /**
   * Adds custom matchers to the {@link https://jasmine.github.io/ Jasmine} environment.
   */
  addCustomMatchers: function () {
    jasmine.addMatchers({
      toContainAll: function(util, customEqualityTesters) {
        return {
          compare: function(actual, expected) {
            return {
              pass: expected.every((item) => util.contains(actual, item, customEqualityTesters))
            };
          }
        };
      }
    });
  },

  /**
   * Converts a {@link https://en.wikipedia.org/wiki/Camel_case Camel} case string to {@link
   * https://en.wikipedia.org/wiki/Snake_case Snake} case.
   *
   * @return {string} The input string in Snake Case format.
   */
  camelCaseToUnderscore: function (str) {
    if (!str) {
      throw new Error('string is undefined');
    }
    const result = str.charAt(0).toUpperCase() + str.slice(1)
      .replace(/([A-Z])/g, '_$1').toUpperCase()
      .replace(' ', '');
    return result;
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
   * @return {object} An object that simulates {@link https://angular-ui.github.io/bootstrap/ UI-Bootstrap} modal.
   */
  fakeModal() {
    return {
      result: {
        then: function(confirmCallback, cancelCallback) {
          //Store the callbacks for later when the user clicks on the OK or Cancel button of the dialog
          this.confirmCallBack = confirmCallback;
          this.cancelCallback = cancelCallback;
        }
      },
      close: function(item) {
        //The user clicked OK on the modal dialog, call the stored confirm callback with the selected item
        this.result.confirmCallBack(item);
      },
      dismiss: function(type) {
        //The user clicked cancel on the modal dialog, call the stored cancel callback
        this.result.cancelCallback(type);
      }
    };
  },

  /**
   * @return {boolean} a random boolean.
   */
  randomBoolean: function () {
    return faker.random.number() === 1;
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
