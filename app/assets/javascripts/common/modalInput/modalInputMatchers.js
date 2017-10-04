/* global angular */

import _ from 'lodash';

const sprintf = require('sprintf-js').sprintf;

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
          message = sprintf('Expected "%s" %s have label be "%s"',
                            angular.mock.dump(element),
                            pass ? 'not to' : 'to',
                            expected);

          return { pass: pass, message: message };
        }
      };
    },
    toHaveInputElementBeFocused: function() {
      return {
        compare: function(actual, expected) {
          var element = actual.find('form').find('input'),
              pass = (element.length === 1) && (element.attr('focus-me') === 'true'),
              message = sprintf('Expected input element %s be valid',
                                angular.mock.dump(element),
                                pass ? 'not to' : 'to',
                                expected);

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
          message = sprintf('Expected "%s" %s have %d input elements',
                            angular.mock.dump(element),
                            pass ? 'not to' : 'to',
                            expected);
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
          message = sprintf('Expected "%s"" type %s be "%s"',
                            angular.mock.dump(element),
                            pass ? 'not to' : 'to',
                            expected);

          return { pass: pass, message: message };
        }
      };
    },
    toHaveValidTextAreaElement: function() {
      return {
        compare: function(actual, expected) {
          var element = actual.find('form').find('textarea'),
              pass,
              message;

          pass = element.length === 1 &&
            (element.attr('focus-me') === 'true') &&
            (element.attr('ng-model') === 'vm.value') &&
            (element.attr('ng-required') === 'vm.options.required');
          message = sprintf('Expected modal %s have a textarea element',
                            angular.mock.dump(element),
                            pass ? 'not to' : 'to',
                            expected);

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
          message = sprintf('Expected modal controller scope "%s" %s have a values "%s"',
                            angular.mock.dump(scope),
                            pass ? 'not to' : 'to',
                            angular.mock.dump(expected));

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
          message = sprintf('Expected modal %s have help blocks', pass ? 'not to' : 'to');
          return { pass: pass, message: message };
        }
      };
    }
  });
}
