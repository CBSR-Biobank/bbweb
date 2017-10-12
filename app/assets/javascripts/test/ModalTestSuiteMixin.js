/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
import _ from 'lodash';
import angular from 'angular';

/* @ngInject */
export default function ModalTestSuiteMixinFactory($rootScope, $animate, $document, TestSuiteMixin) {

  return _.extend(
    {
      modalElementFind: modalElementFind,
      flush: flush,
      dismiss: dismiss,
      addModalMatchers: addModalMatchers
    },
    TestSuiteMixin
  );


  function modalElementFind () {
    return $document.find('body > div.modal');
  }

  function flush () {
    $animate.flush();
    $rootScope.$digest();
    $animate.flush();
    $rootScope.$digest();
  }

  function dismiss (modal = this.modal, reason, noFlush) {
    var closed;
    closed = modal.dismiss(reason);
    $rootScope.$digest();
    if (!noFlush) {
      this.flush();
    }
    return closed;
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
