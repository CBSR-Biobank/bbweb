/* global angular */

import _ from 'lodash';

/**
 * Jasmine matchers to test the modalInput Module.
 *
 * @return {void} nothing.
 */
export default function modalInputMatcher () {
  jasmine.addMatchers({
    toHaveLabelStartWith: function() {
      return {
        compare: function(actual, expected) {
          var element = actual.find('label'),
              pass,
              message;

          expected = expected || '';
          pass    = element.text().slice(0, expected.length) === expected;
          message = `Expected "${angular.mock.dump(element)}" ${pass ? 'not to' : 'to'} have label be "${expected}"`;

          return { pass: pass, message: message };
        }
      };
    },
    toHaveInputElementBeFocused: function() {
      return {
        compare: function(actual) {
          var element = actual.find('form').find('input'),
              pass = (element.length === 1) && (element.attr('focus-me') === 'true'),
              message = 'Expected input element to be focused';

          return { pass: pass, message: message };
        }
      };
    },
    toHaveInputs: function() {
      return {
        compare: function(actual, expected) {
          var pass, message, element = actual.find('form').find('input');
          expected = expected || 0;
          pass = (element.length === expected);
          message = `Expected "${angular.mock.dump(element)}" %s have ${expected} input elements`;
          return { pass: pass, message: message };
        }
      };
    },
    toHaveInputElementTypeAttrBe: function() {
      return {
        compare: function(actual, expected) {
          var element = actual.find('form').find('input'),
              pass,
              message;

          expected = expected || '';
          pass = (element.length === 1) && (element.attr('type') === expected);
          message = `Expected "${angular.mock.dump(element)}"" type ${pass ? 'not to' : 'to'} be "${expected}"`;

          return { pass: pass, message: message };
        }
      };
    },
    toHaveValidTextAreaElement: function() {
      return {
        compare: function(actual) {
          var element = actual.find('form').find('textarea'),
              pass,
              message;

          pass = element.length === 1 &&
            (element.attr('focus-me') === 'true') &&
            (element.attr('ng-model') === 'vm.value') &&
            (element.attr('ng-required') === 'vm.options.required');
          message = `Expected modal ${angular.mock.dump(element)} have a textarea element`;

          return { pass: pass, message: message };
        }
      };
    },
    toHaveValuesInControllerScope: function() {
      return {
        compare: function(actual, expected) {
          var scope = actual.scope().vm,
              pass,
              message;

          expected = expected || {};
          pass = _.chain(expected).keys().every(checkScopeValue).value();
          message = `Expected modal controller scope "${angular.mock.dump(scope)}" ${pass ? 'not to' : 'to'} have a values "${expected}"`;

          return { pass: pass, message: message };

          function checkScopeValue(key) {
            return _.has(scope, key) && _.isEqual(expected[key], scope[key]);
          }
        }
      };
    },
    toHaveHelpBlocks: function() {
      return {
        compare: function(actual) {
          var element = actual.find('form').find('.help-block'),
              pass,
              message;

          pass = (element.length > 0);
          message = `Expected modal ${pass ? 'not to' : 'to'} have help blocks`;
          return { pass: pass, message: message };
        }
      };
    }
  });
}
