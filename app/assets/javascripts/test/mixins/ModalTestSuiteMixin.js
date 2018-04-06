/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import angular from 'angular';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.ModalTestSuiteMixin
 */
let ModalTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([ '$rootScope', '$document', '$animate' ]);
    TestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  /**
   * Finds the modal DOM element.
   */
  modalElementFind : function () {
    return this.$document.find('body > div.modal');
  },

  /**
   * Closes the modal.
   *
   * Same as pressing the `OK` button.
   */
  close: function (modal = this.modal, reason, noFlush) {
    const closed = modal.close(reason);
    this.$rootScope.$digest();
    if (!noFlush) {
      this.flush.call(this);
    }
    return closed;
  },

  /**
   * Dismisses the modal.
   *
   * Same as pressing the `Cancel` button.
   */
  dismiss: function (modal = this.modal, reason, noFlush) {
    const closed = modal.dismiss(reason);
    this.$rootScope.$digest();
    if (!noFlush) {
      this.flush.call(this);
    }
    return closed;
  },

  /**
   * Flushes pending callbacks and animation frames to either start an animation or conclude an animation.
   */
  flush: function () {
    this.$animate.flush();
    this.$rootScope.$digest();
    this.$animate.flush();
    this.$rootScope.$digest();
  },

  /**
   * Add custom Jasmine matchers that inspect the DOM for modal elements.
   */
  addModalMatchers : function () {
    jasmine.addMatchers({
      toHaveModalsOpen,
      toHaveModalTitle,
      toHaveModalBody
    });

    function toHaveModalsOpen(util, customEqualityTesters) {
      return {
        compare: (actual, expected) => {
          const modalDomEls = actual.find('body > div.modal');
          const pass = util.equals(modalDomEls.length, expected, customEqualityTesters);
          const message =
                `Expected document ${pass ? 'not to' : 'to'} have "${expected}" modals opened.`;

          return { pass: pass, message: message };
        }
      };
    }

    function toHaveModalTitle(util, customEqualityTesters) {
      return {
        compare: (actual, expected) => {
          const element = actual.find('.modal-title');
          const pass    = util.equals(element.text(), expected, customEqualityTesters);
          const elementDump = angular.mock.dump(element);
          const expectedDump = angular.mock.dump(expected);
          const message =
                `Expected "${elementDump}" ${pass ? 'not to' : 'to'} have title be "${expectedDump}"`;

          return { pass: pass, message: message };
        }
      };
    }

    function toHaveModalBody(util, customEqualityTesters) {
      return {
        compare: (actual, expected) => {
          const element = actual.find('.modal-body > div');
          const pass = util.equals(element.text(), expected, customEqualityTesters);
          const elementDump = angular.mock.dump(element);
          const expectedDump = angular.mock.dump(expected);
          const message = `Expected "${elementDump}" ${pass ? 'not to' : 'to'} be "${expectedDump}"`;

          return { pass: pass, message: message };
        }
      };
    }

  }

}

ModalTestSuiteMixin = Object.assign({}, TestSuiteMixin, ModalTestSuiteMixin);

export { ModalTestSuiteMixin };
export default () => {};
