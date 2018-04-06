/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ModalTestSuiteMixin } from 'test/mixins/ModalTestSuiteMixin';
import ngModule from '../../index'

describe('asyncInputModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ModalTestSuiteMixin)

      this.injectDependencies('$q',
                              '$rootScope',
                              '$document',
                              '$uibModal',
                              'asyncInputModal',
                              'Factory')

      this.openModal =
        (heading          = this.Factory.stringNext(),
         label            = this.Factory.stringNext(),
         placeholder      = this.Factory.stringNext(),
         noResultsMessage = this.Factory.stringNext(),
         getResults       = jasmine.createSpy('getResults').and.returnValue(this.$q.when([])),
         resultFunc       = angular.noop,
         closedledFunc    = angular.noop) => {
          this.asyncInputModal.open(heading, label, placeholder, noResultsMessage, getResults)
            .result.then(resultFunc, closedledFunc)
          this.$rootScope.$digest()
          this.modalElement = this.modalElementFind()
          this.scope = this.modalElement.scope()
        }

      this.dismiss = (scope, noFlush) => {
        scope = scope || this.scope
        scope.modalOptions.close()
        this.$rootScope.$digest()
        if (!noFlush) {
          this.flush()
        }
      }

      this.addModalMatchers()
    })
  })

  describe('for service', function() {

    it('service has correct functions', function() {
      expect(this.asyncInputModal.open).toBeFunction()
    })

    it('modal can be opened', function() {
      spyOn(this.$uibModal, 'open').and.returnValue(null)
      this.asyncInputModal.open()
      expect(this.$uibModal.open).toHaveBeenCalled()
    })

  })

  describe('when modal is opened', function() {

    afterEach(function () {
      const body = this.$document.find('body')
      body.find('div.modal').remove()
      body.find('div.modal-backdrop').remove()
      body.removeClass('modal-open')
      this.$document.off('keydown')
    })

    it('has valid scope', function() {
      const heading          = this.Factory.stringNext(),
            label            = this.Factory.stringNext(),
            placeholder      = this.Factory.stringNext(),
            noResultsMessage = this.Factory.stringNext()

      this.openModal(heading, label, placeholder, noResultsMessage)

      expect(this.scope.vm.heading).toBe(heading)
      expect(this.scope.vm.label).toBe(label)
      expect(this.scope.vm.placeholder).toBe(placeholder)
      expect(this.scope.vm.noResultsMessage).toBe(noResultsMessage)
    })

    it('async function is invoked', function() {
      let getValuesFuncInvoked = false
      const heading          = this.Factory.stringNext(),
            label            = this.Factory.stringNext(),
            placeholder      = this.Factory.stringNext(),
            noResultsMessage = this.Factory.stringNext(),
            getValues        = jasmine.createSpy().and.returnValue(this.$q.when([])),
            viewValue        = this.Factory.stringNext()

      this.openModal(heading, label, placeholder, noResultsMessage, getValues)
      this.scope.vm.getValues(viewValue).then(() => {
        expect(getValues).toHaveBeenCalledWith(viewValue)
        getValuesFuncInvoked = true
      })
      this.scope.$digest()
      expect(getValuesFuncInvoked).toBeTrue()
    })

    it('user can select a value', function() {
      const heading          = this.Factory.stringNext(),
            label            = this.Factory.stringNext(),
            placeholder      = this.Factory.stringNext(),
            noResultsMessage = this.Factory.stringNext(),
            selectedValue    = this.Factory.stringNext()

      this.openModal(heading, label, placeholder, noResultsMessage)
      this.scope.vm.valueSelected(selectedValue)
      expect(this.scope.vm.value).toEqual(selectedValue)
    })

    it('modal returns selected value', function() {
      let resultFuncInvoked = false
      const heading          = this.Factory.stringNext(),
            label            = this.Factory.stringNext(),
            placeholder      = this.Factory.stringNext(),
            noResultsMessage = this.Factory.stringNext(),
            selectedValue    = this.Factory.stringNext(),
            resultFunc       = (value) => {
              expect(value).toEqual(selectedValue)
              resultFuncInvoked = true
            }

      this.openModal(heading, label, placeholder, noResultsMessage, undefined, resultFunc)
      this.scope.vm.valueSelected(selectedValue)
      this.scope.vm.okPressed()
      this.scope.$digest()
      expect(resultFuncInvoked).toBeTrue()
    })

    it('user can press the close button', function() {
      let closedFuncInvoked = false
      const heading          = this.Factory.stringNext(),
            label            = this.Factory.stringNext(),
            placeholder      = this.Factory.stringNext(),
            noResultsMessage = this.Factory.stringNext(),
            selectedValue    = this.Factory.stringNext(),
            closedFunc       = () => {
              closedFuncInvoked = true
            }

      this.openModal(heading, label, placeholder, noResultsMessage, undefined, undefined, closedFunc)
      this.scope.vm.valueSelected(selectedValue)
      this.scope.vm.closePressed()
      this.scope.$digest()
      expect(closedFuncInvoked).toBeTrue()
    })

  })

})
