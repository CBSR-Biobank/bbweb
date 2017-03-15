/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('dateTimePickerComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');

      this.createScope = function (label, defaultValue, required, onEdit, labelCols, inputCols) {
        var openTag = '<date-time-picker label="' + label + '"' +
            '                            default-value="vm.defaultValue"' +
            '                            required="vm.required"' +
            '                            on-edit="vm.onEdit"',
            closeTag = '><date-time-picker>';

        if (labelCols) {
          openTag += ' label-cols="' + labelCols + '"';
        }

        if (inputCols) {
          openTag += ' input-cols="' + inputCols + '"';
        }

        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          openTag + closeTag,
          {
            defaultValue: defaultValue,
            required:     required,
            onEdit:       onEdit
          },
          'dateTimePicker');
      };
    }));

    it('has valid scope', function() {
      var label        = this.factory.stringNext(),
          defaultValue = new Date(),
          required     = true,
          onEdit       = jasmine.createSpy().and.returnValue(null),
          labelCols    = 'col-md-2',
          inputCols    = 'col-md-10';

      this.createScope(label, defaultValue, required, onEdit, labelCols, inputCols);
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

      this.createScope(this.factory.stringNext(), defaultValue, true, onEdit);
      this.controller.onChange();
      this.scope.$digest();
      expect(onEdit).toHaveBeenCalledWith(defaultValue);
    });


  });

});
