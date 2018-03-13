/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

/* @ngInject */
function ModalTestSuiteMixin($rootScope, $document, $animate, TestSuiteMixin) {

  return Object.assign({ modalElementFind, close, dismiss, flush, addModalMatchers }, TestSuiteMixin);

  function modalElementFind () {
    return $document.find('body > div.modal');
  }

  function close(modal = this.modal, reason, noFlush) {
    const closed = modal.close(reason);
    $rootScope.$digest();
    if (!noFlush) {
      flush.call(this);
    }
    return closed;
  }

  function dismiss(modal = this.modal, reason, noFlush) {
    const closed = modal.dismiss(reason);
    $rootScope.$digest();
    if (!noFlush) {
      flush.call(this);
    }
    return closed;
  }

  function flush() {
    $animate.flush();
    $rootScope.$digest();
    $animate.flush();
    $rootScope.$digest();
  }

  /**
   * jasmine matchers that inspect the DOM for modal elements.
   */
  function addModalMatchers () {
    jasmine.addMatchers({
      toHaveModalsOpen: function(util, customEqualityTesters) {
        return {
          compare: function(actual, expected) {
            var modalDomEls = actual.find('body > div.modal'),
                pass        = util.equals(modalDomEls.length, expected, customEqualityTesters),
                message     = `Expected document ${pass ? 'not to' : 'to'} have "${expected}" modals opened.`;

            return { pass: pass, message: message };
          }
        };
      },
      toHaveModalTitle: function(util, customEqualityTesters) {
        return {
          compare: function(actual, expected) {
            var element = actual.find('.modal-title'),
                pass    = util.equals(element.text(), expected, customEqualityTesters),
                message = `Expected "${angular.mock.dump(element)}" ${pass ? 'not to' : 'to'} have title be "${expected}"`;

            return { pass: pass, message: message };
          }
        };
      },
      toHaveModalBody: function(util, customEqualityTesters) {
        return {
          compare: function(actual, expected) {
            var element = actual.find('.modal-body > div'),
                pass    = util.equals(element.text(), expected, customEqualityTesters),
                message = `Expected "${angular.mock.dump(element)}" ${pass ? 'not to' : 'to'} be "${expected}"`;

            return { pass: pass, message: message };
          }
        };
      }
    });
  }

}

export default ngModule => ngModule.service('ModalTestSuiteMixin', ModalTestSuiteMixin)
