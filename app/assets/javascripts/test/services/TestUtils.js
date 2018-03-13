/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import _ from 'lodash';
import faker  from 'faker';

/**
 * Various utility functions that can be used in test cases.
 *
 * @memberOf test.services
 */
class TestUtils {

  /**
   * @return {boolean} a random boolean.
   */
  randomBoolean() {
    return faker.random.number() === 1;
  }

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
  }

  /**
   * Adds custom matchers to the {@link https://jasmine.github.io/ Jasmine} environment.
   */
  addCustomMatchers() {
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
  }

  /**
   * Converts a {@link https://en.wikipedia.org/wiki/Camel_case Camel} case string to {@link
   * https://en.wikipedia.org/wiki/Snake_case Snake} case.
   *
   * @return {string} The input string in Snake Case format.
   */
  camelCaseToUnderscore(str) {
    var result;
    if (_.isUndefined(str)) {
      throw new Error('string is undefined');
    }
    result = str.charAt(0).toUpperCase() + str.slice(1)
      .replace(/([A-Z])/g, '_$1').toUpperCase()
      .replace(' ', '');
    return result;
  }

}

export default ngModule => ngModule.service('TestUtils', TestUtils)
