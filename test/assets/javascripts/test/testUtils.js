/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      _       = require('lodash'),
      faker   = require('faker'),
      sprintf = require('sprintf').sprintf;

  /**
   * Description
   */
  function testUtils() {
    var service = {
      uuid:                    uuid,
      randomBoolean:           randomBoolean,
      fakeModal:               fakeModal,
      addCustomMatchers:       addCustomMatchers,
      camelCaseToUnderscore:   camelCaseToUnderscore,
      jasmineAddModalMatchers: jasmineAddModalMatchers
    };
    return service;

    //-------

    /**
     * Taken from fixer version 2.1.2. When karma-fixer uses the same version this
     * function can be removed.
     */
    function uuid() {
      var RFC4122_TEMPLATE = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
      var replacePlaceholders = function (placeholder) {
        /* jshint bitwise: false */
        var random = Math.random() * 16 | 0;
        var value = (placeholder === 'x') ? random : (random &0x3 | 0x8);
        /* jshint bitwise: true */
        return value.toString(16);
      };
      return RFC4122_TEMPLATE.replace(/[xy]/g, replacePlaceholders);
    }

    function randomBoolean() {
      return faker.random.number() === 1;
    }

    function fakeModal() {
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

    function addCustomMatchers() {
      jasmine.addMatchers({
        toContainAll: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              return { pass: _.each(expected, function (item) {
                return util.contains(actual, item, customEqualityTesters); })
                };
            }
          };
        }
      });
    }

    function camelCaseToUnderscore(str) {
      var result;
      if (_.isUndefined(str)) {
        throw new Error('string is undefined');
      }
      result = str.charAt(0).toUpperCase() + str.slice(1)
        .replace(/([A-Z])/g, '_$1').toUpperCase()
        .replace(' ', '');
      return result;
    }

    /**
     * jasmine matchers that inspect the DOM for modal elements.
     */
    function jasmineAddModalMatchers() {
      jasmine.addMatchers({
        toHaveModalsOpen: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var modalDomEls = actual.find('body > div.modal'),
                  pass        = util.equals(modalDomEls.length, expected, customEqualityTesters),
                  message     = sprintf('Expected "%s" %s have "%s" modals opened.',
                                        angular.mock.dump(modalDomEls),
                                        pass ? 'not to' : 'to',
                                        expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveTitle: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('.modal-title'),
                  pass    = util.equals(element.text(), expected, customEqualityTesters),
                  message = sprintf('Expected "%s" %s have title be "%s"',
                                    angular.mock.dump(element),
                                    pass ? 'not to' : 'to',
                                    expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveBody: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('.modal-body'),
                  pass    = util.equals(element.text(), expected, customEqualityTesters),
                  message = sprintf('Expected "%s" %s have be "%s"',
                                    angular.mock.dump(element),
                                    pass ? 'not to' : 'to',
                                    expected);

              return { pass: pass, message: message };
            }
          };
        }
      });
    }

  }

  return testUtils;
});
