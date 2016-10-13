/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      sprintf = require('sprintf').sprintf;

  ModalTestSuiteMixinFactory.$inject = [
    '$rootScope',
    '$animate',
    '$document',
    'TestSuiteMixin'
  ];

  function ModalTestSuiteMixinFactory($rootScope, $animate, $document, TestSuiteMixin) {

    /**
     * A mixin that has methods for testing services that open a modal.
     */
    function ModalTestSuiteMixin() {
      TestSuiteMixin.call(this);
    }

    ModalTestSuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    ModalTestSuiteMixin.prototype.constructor = ModalTestSuiteMixin;

    ModalTestSuiteMixin.prototype.modalElementFind = function() {
      return $document.find('body > div.modal');
    };

    ModalTestSuiteMixin.prototype.flush = function () {
      $animate.flush();
      $rootScope.$digest();
      $animate.flush();
      $rootScope.$digest();
    };

    ModalTestSuiteMixin.prototype.dismiss = function (modal, reason, noFlush) {
      var closed;
      modal = modal || this.modal;
      closed = modal.dismiss(reason);
      $rootScope.$digest();
      if (!noFlush) {
        this.flush();
      }
      return closed;
    };

    /**
     * jasmine matchers that inspect the DOM for modal elements.
     */
    ModalTestSuiteMixin.prototype.addModalMatchers = function() {
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
        toHaveModalTitle: function(util, customEqualityTesters) {
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
        toHaveModalBody: function(util, customEqualityTesters) {
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
    };

    return ModalTestSuiteMixin;
  }

  return ModalTestSuiteMixinFactory;
});
