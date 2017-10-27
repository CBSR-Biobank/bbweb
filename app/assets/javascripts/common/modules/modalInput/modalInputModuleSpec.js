/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import faker  from 'faker';
import modalInputMatchers from '../../../test/matchers/modalInputMatchers';
import moment from 'moment';

xdescribe('modalInputModule', function() {

  angular.mock.module.sharedInjector();

  beforeAll(angular.mock.module(
    'ngAnimateMock',
    'biobankApp',
    'biobank.test',
    function($exceptionHandlerProvider) {
      $exceptionHandlerProvider.mode('log');
     }
  ));

  beforeEach(() => {
    //angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin) {
      _.extend(this, ModalTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$exceptionHandler',
                              '$animate',
                              '$document',
                              '$timeout',
                              'modalInput',
                              'Factory');


      this.addModalMatchers();
      modalInputMatchers();
      this.title = this.Factory.stringNext();
      this.label = this.Factory.stringNext();

      this.openModal =
        (modalInputFunc,
         defaultValue,
         title = this.title,
         label = this.label,
         options) => {
           this.modal = modalInputFunc(title, label, defaultValue, options);
           this.modal.opened.catch(angular.noop);
           this.modal.result.catch(angular.noop);
           this.modal.result
             .then(angular.noop)
             .catch(angular.noop);
           this.$rootScope.$digest();
           this.$animate.flush();
           this.modalElement = this.modalElementFind();
           this.scope = this.modalElement.scope();
         };
    });
  });

  afterEach(function () {
    this.modalElement.remove();
    const body = this.$document.find('body');
    body.find('div.modal').remove();
    body.find('div.modal-backdrop').remove();
    body.removeClass('modal-open');
    this.$document.off('keydown');
  });

  describe('boolean modal', function () {

    it('has valid elements and scope', function() {
      this.defaultValue = false;
      this.openModal(this.modalInput.boolean,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(2);
      expect(this.modalElement).toHaveValuesInControllerScope(
        { value: this.defaultValue, options: undefined });

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when value is required and both values are unchecked', function() {
      this.defaultValue = false;
      this.openModal(this.modalInput.boolean,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      this.scope.form.value.$setViewValue(null);
      expect(this.scope.form.$valid).toBe(false);

      // check that it becomes valid
      this.scope.form.value.$setViewValue(false);
      expect(this.scope.form.$valid).toBe(true);

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('dateTime modal', function() {

    beforeEach(function () {
      this.date = faker.date.recent(10);
      this.date.setSeconds(0);
      this.date.setMilliseconds(0);
      this.defaultValue = moment(this.date).format();
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.dateTime,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveValuesInControllerScope({ options: undefined });
      expect(this.modalElement).not.toHaveHelpBlocks();

      expect(this.modalElement.scope().vm.value).toBeDate();
      expect(new Date(this.modalElement.scope().vm.value)).toEqual(this.date);

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when value is required and value is blank', function() {
      this.openModal(this.modalInput.boolean,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });

      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('email modal', function() {

    beforeEach(function () {
      this.defaultValue = faker.internet.email();
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.email,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('email');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.email,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is an invalid email', function() {
      this.openModal(this.modalInput.email,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('xxx');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('natural number modal', function() {

    beforeEach(function () {
      this.defaultValue = 10;
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.naturalNumber,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('number');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.naturalNumber,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is an invalid number', function() {
      this.openModal(this.modalInput.naturalNumber,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('-1');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('number modal', function() {

    beforeEach(function () {
      this.defaultValue = 10;
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.number,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('number');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.number,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is not a number', function() {
      this.openModal(this.modalInput.number,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('xxx');
      expect(this.scope.form.$valid).toBe(false);

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('password modal', function() {

    beforeEach(function () {
      this.defaultValue = 10;
    });

    it('has valid elements and scope', function() {
      var inputs;

      this.openModal(this.modalInput.password,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveInputs(3);
      expect(this.modalElement).toHaveValuesInControllerScope({ value: { } });
      expect(this.modalElement).not.toHaveHelpBlocks();

      inputs = this.modalElement.find('form').find('input');
      expect(inputs.attr('focus-me')).toBe('true');

      _.range(inputs.length).forEach((index) => {
        const input = inputs.eq(index);
        expect(angular.element(input).attr('type')).toBe('password');
      });

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when new password and confirm password do not match', function() {
      this.openModal(this.modalInput.password,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.newPassword.$setViewValue('abcabcabc');
      this.scope.form.confirmPassword.$setViewValue('xyzxyzxyz');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('positive float modal', function() {

    beforeEach(function () {
      this.defaultValue = 10;
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.positiveFloat,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('number');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.positiveFloat,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is not a number', function() {
      this.openModal(this.modalInput.positiveFloat,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('xxx');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is a negative number', function() {
      this.openModal(this.modalInput.positiveFloat,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('-1.00');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('select modal', function() {

    beforeEach(function () {
      this.options = _.range(3).map(() => faker.lorem.word());
      this.defaultValue = this.options[0];
    });

    it('has valid elements and scope', function() {
      var self = this,
          optionElements;

      self.openModal(self.modalInput.select,
                     self.defaultValue,
                     self.title,
                     self.label,
                     { selectOptions: self.options });

      expect(self.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(self.title);
      expect(this.modalElement).toHaveLabelStartWith(self.label);
      expect(this.modalElement).toHaveValuesInControllerScope({ value: self.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      optionElements = this.modalElement.find('form').find('option');
      _.range(optionElements.length).forEach((index) => {
        const text = angular.element(optionElements.eq(index)).text();
        if (text !== '-- make a selection --') {
          expect(self.options).toContain(text);
        }
      });

      this.dismiss(this.modal, 'closed in test');
      expect(self.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when a value is required and nothing selected', function() {
      this.openModal(this.modalInput.select,
                     this.defaultValue,
                     this.title,
                     this.label,
                     {
                       required: true,
                       selectOptions: this.options
                     });

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });

      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('select multiple modal', function() {

    beforeEach(function () {
      expect(this.$document).toHaveModalsOpen(0);
      this.options = _.range(3).map(() => faker.random.word());
      this.defaultValue = [ this.options[0] ];
    });

    it('has valid elements and scope', function() {
      var labelElements;

      this.openModal(this.modalInput.selectMultiple,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { selectOptions: this.options });

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(this.options.length);

      labelElements = this.modalElement.find('form').find('label');
      _.range(labelElements.length).forEach((index) => {
        if (index === 0) { return; } // skip the first label since it's for the group
        const element = labelElements.eq(index);
        expect(this.options).toContain(angular.element(element).text().trim());
      });

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid if a value is required and nothing selected', function() {
      this.openModal(this.modalInput.selectMultiple,
                     this.defaultValue,
                     this.title,
                     this.label,
                     {
                       required: true,
                       selectOptions: this.options
                     });

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(this.options.length);

      this.scope.vm.value.forEach((value) => {
        value.checked = true;
      });
      this.scope.$digest();
      this.scope.vm.value.forEach((value) => {
        value.checked = false;
      });
      this.scope.$digest();
      this.scope.form.selectValue.$setViewValue(false);
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('has a help block when required', function() {
      this.openModal(this.modalInput.selectMultiple,
                     [],
                     this.title,
                     this.label,
                     {
                       required: true,
                       selectOptions: this.options
                     });

      expect(this.modalElement).toHaveHelpBlocks();
      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('throws an exception if select options are not provided ', function() {
      var err = 'select options not provided';
      this.modalInput.selectMultiple('', '', '', { });
      this.$rootScope.$digest();
      expect(this.$exceptionHandler.errors[0][0].message).toEqual(err);
    });

    it('throws an exception if default value is not an array', function() {
      var err = 'defaultValue is not an array';
      this.modalInput.selectMultiple('', '', '', { selectOptions: this.options });
      this.$rootScope.$digest();
      expect(this.$exceptionHandler.errors[1][0].message).toEqual(err);
    });

  });

  describe('text modal', function() {

    beforeEach(function () {
      expect(this.$document).toHaveModalsOpen(0);
      this.defaultValue = this.Factory.stringNext();
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.text,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('text');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.text,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when minimum length is not met', function() {
      var minLength = 2;

      this.openModal(this.modalInput.text,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { minLength: minLength });

      expect(this.modalElement).toHaveValuesInControllerScope({ options: { minLength: minLength } });
      this.scope.form.value.$setViewValue('x');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('modal should be closed when OK button is pressed', function() {
      this.openModal(this.modalInput.text,
                     this.Factory.stringNext(),
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.okPressed();
      this.$rootScope.$digest();

      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('modal should be closed when Cancel button is pressed', function() {
      this.openModal(this.modalInput.text,
                     this.Factory.stringNext(),
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.closePressed();
      this.$rootScope.$digest();

      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('textArea modal', function() {

    beforeEach(function () {
      expect(this.$document).toHaveModalsOpen(0);
      this.defaultValue = faker.lorem.sentences(4);
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.textArea,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveValidTextAreaElement();
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.textArea,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  describe('url modal', function() {

    beforeEach(function () {
      expect(this.$document).toHaveModalsOpen(0);
      this.defaultValue = faker.image.imageUrl();
    });

    it('has valid elements and scope', function() {
      this.openModal(this.modalInput.url,
                     this.defaultValue,
                     this.title,
                     this.label);

      expect(this.$document).toHaveModalsOpen(1);

      expect(this.modalElement).toHaveModalTitle(this.title);
      expect(this.modalElement).toHaveLabelStartWith(this.label);
      expect(this.modalElement).toHaveInputs(1);
      expect(this.modalElement).toHaveInputElementBeFocused();
      expect(this.modalElement).toHaveInputElementTypeAttrBe('url');
      expect(this.modalElement).toHaveValuesInControllerScope({ value: this.defaultValue });
      expect(this.modalElement).not.toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when using required option and input is empty', function() {
      this.openModal(this.modalInput.url,
                     this.defaultValue,
                     this.title,
                     this.label,
                     { required: true });
      expect(this.modalElement).toHaveValuesInControllerScope({ options: { required: true } });
      this.scope.form.value.$setViewValue('');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss(this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('form is invalid when input is an invalid email', function() {
      this.openModal(this.modalInput.url,
                     this.defaultValue,
                     this.title,
                     this.label);
      this.scope.form.value.$setViewValue('xxx');
      expect(this.scope.form.$valid).toBe(false);
      expect(this.modalElement).toHaveHelpBlocks();

      this.dismiss( this.modal, 'closed in test');
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

});
