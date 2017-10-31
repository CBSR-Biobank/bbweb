/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('dateTimePickerComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Factory');

      this.createController = (label, defaultValue, required, onEdit, labelCols, inputCols) => {
        var labelAttrs = (labelCols) ? `label-cols="${labelCols}"` : '',
            inputAttrs = (inputCols) ? `input-cols="${inputCols}"` : '';

        ComponentTestSuiteMixin.createController.call(
          this,
          `<date-time-picker label="${label}"
              default-value="vm.defaultValue"
              required="vm.required"
              on-edit="vm.onEdit"
              ${labelAttrs}
              ${inputAttrs}>
           </date-time-picker>`,
          {
            defaultValue: defaultValue,
            required:     required,
            onEdit:       onEdit
          },
          'dateTimePicker');
      };
    });
  });

  it('has valid scope', function() {
    var label        = this.Factory.stringNext(),
        defaultValue = new Date(),
        required     = true,
        onEdit       = jasmine.createSpy().and.returnValue(null),
        labelCols    = 'col-md-2',
        inputCols    = 'col-md-10';

    this.createController(label, defaultValue, required, onEdit, labelCols, inputCols);
    expect(this.controller.label).toEqual(label);
    expect(this.controller.defaultValue).toEqual(defaultValue);
    expect(this.controller.required).toEqual(required);
    expect(this.controller.labelCols).toEqual(labelCols);
    expect(this.controller.inputCols).toEqual(inputCols);
    expect(this.controller.open).toBeFalse();
  });

  it('on change invokes callback', function() {
    var defaultValue = new Date(),
        onEdit       = jasmine.createSpy().and.returnValue(null);

    this.createController(this.Factory.stringNext(), defaultValue, true, onEdit);
    this.controller.onChange();
    this.scope.$digest();
    expect(onEdit).toHaveBeenCalledWith(defaultValue);
  });

});
