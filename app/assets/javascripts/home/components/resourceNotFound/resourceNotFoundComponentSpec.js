/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../../index'

describe('resourceNotFoundComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('Factory')

      this.createController = (errMessage = this.Factory.stringNext()) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          `<resource-not-found err-message="${errMessage}"></resource-not-found>`,
          undefined,
          'resourceNotFound');
    })
  })

  it('has valid scope', function() {
    const errMessage = this.Factory.stringNext()
    this.createController(errMessage)
    expect(this.controller.errMessage).toBe(errMessage)
  })

})
