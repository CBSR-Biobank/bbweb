/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: debouncedTextInput', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');
      this.createController = (label, value = this.Factory.stringNext()) => {
        this.onValueChanged = jasmine.createSpy().and.returnValue(null);
        ComponentTestSuiteMixin.createController.call(
          this,
          `<debounced-text-input
              label="${label}"
              value="vm.value"
              on-value-changed="vm.onValueChanged"
           </debounced-text-input>`,
          {
            value: value,
            onValueChanged: this.onValueChanged
          },
          'debouncedTextInput');
      };
    });
  });

  it('label is valid', function() {
    var label = this.Factory.stringNext();
    this.createController(label);
    expect(this.controller.label).toBe(label);
  });

  it('changes are applied when scope variables are updated', function() {
    var newValue = this.Factory.stringNext();
    this.createController(this.Factory.stringNext());
    this.scope.vm.value = newValue;
    this.scope.$digest();
    expect(this.controller.value).toBe(newValue);
  });

  it('function is invoked when changes are made to the input', function() {
    var newValue = this.Factory.stringNext();
    this.createController(this.Factory.stringNext());
    this.scope.vm.value = newValue;
    this.scope.$digest();
    this.controller.updated();
    expect(this.onValueChanged).toHaveBeenCalledWith(newValue);
  });

});
