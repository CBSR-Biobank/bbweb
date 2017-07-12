/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      mocks   = require('angularMocks'),
      _       = require('lodash'),
      moment  = require('moment'),
      faker   = require('faker'),
      sprintf = require('sprintf-js').sprintf;

  function SuiteMixinFactory(ModalTestSuiteMixin) {

    function SuiteMixin() {
      ModalTestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(ModalTestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.openModal = function (modalInputFunc, defaultValue, title, label, options) {
      title = title || this.factory.stringNext();
      label = label || this.factory.stringNext();
      this.modal = modalInputFunc(title, label, defaultValue, options);
      this.modal.result.then(function () {}, function () {});
      this.$rootScope.$digest();
      this.modalElement = this.modalElementFind();
      this.scope = this.modalElement.scope();
    };

    SuiteMixin.prototype.suiteAddMatchers = function () {
      this.addModalMatchers();

      jasmine.addMatchers({
        toHaveLabelStartWith: function() {
          return {
            compare: function(actual, expected) {
              var element = actual.find('label'),
                  pass,
                  message;

              expected = expected || '';
              pass    = element.text().slice(0, expected.length) === expected;
              message = sprintf('Expected "%s" %s have label be "%s"',
                                angular.mock.dump(element),
                                pass ? 'not to' : 'to',
                                expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputElementBeFocused: function() {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('input'),
                  pass = (element.length === 1) && (element.attr('focus-me') === 'true'),
                  message = sprintf('Expected input element %s be valid',
                                    angular.mock.dump(element),
                                    pass ? 'not to' : 'to',
                                    expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputs: function() {
          return {
            compare: function(actual, expected) {
              var pass, message, element = actual.find('form').find('input');
              expected = expected || 0;
              pass = (element.length === expected);
              message = sprintf('Expected "%s" %s have %d input elements',
                                angular.mock.dump(element),
                                pass ? 'not to' : 'to',
                                expected);
              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputElementTypeAttrBe: function() {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('input'),
                  pass,
                  message;

              expected = expected || '';
              pass = (element.length === 1) && (element.attr('type') === expected);
              message = sprintf('Expected "%s"" type %s be "%s"',
                                angular.mock.dump(element),
                                pass ? 'not to' : 'to',
                                expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveValidTextAreaElement: function() {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('textarea'),
                  pass,
                  message;

              pass = element.length === 1 &&
                (element.attr('focus-me') === 'true') &&
                (element.attr('ng-model') === 'vm.value') &&
                (element.attr('ng-required') === 'vm.options.required');
              message = sprintf('Expected modal %s have a textarea element',
                                angular.mock.dump(element),
                                pass ? 'not to' : 'to',
                                expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveValuesInControllerScope: function() {
          return {
            compare: function(actual, expected) {
              var scope = actual.scope().vm,
                  pass,
                  message;

              expected = expected || {};
              pass = _.chain(expected).keys().every(checkScopeValue).value();
              message = sprintf('Expected modal controller scope "%s" %s have a values "%s"',
                                angular.mock.dump(scope),
                                pass ? 'not to' : 'to',
                                angular.mock.dump(expected));

              return { pass: pass, message: message };

              function checkScopeValue(key) {
                return _.has(scope, key) && _.isEqual(expected[key], scope[key]);
              }
            }
          };
        },
        toHaveHelpBlocks: function() {
          return {
            compare: function(actual) {
              var element = actual.find('form').find('.help-block'),
                  pass,
                  message;

              pass = (element.length > 0);
              message = sprintf('Expected modal %s have help blocks', pass ? 'not to' : 'to');
              return { pass: pass, message: message };
            }
          };
        }
      });
    };

    return SuiteMixin;
  }

  describe('modalInputModule', function() {

    mocks.module.sharedInjector();

    beforeAll(mocks.module(
      'ngAnimateMock',
      'biobankApp',
      'biobank.test',
      function($exceptionHandlerProvider) {
        $exceptionHandlerProvider.mode('log');
      }
    ));

    beforeEach(inject(function(ModalTestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(ModalTestSuiteMixin);

      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$exceptionHandler',
                              '$animate',
                              '$document',
                              'modalInput',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/common/modalInput/modalInput.html',
        '/assets/javascripts/common/modalInput/boolean.html',
        '/assets/javascripts/common/modalInput/dateTime.html',
        '/assets/javascripts/common/modalInput/email.html',
        '/assets/javascripts/common/modalInput/naturalNumber.html',
        '/assets/javascripts/common/modalInput/number.html',
        '/assets/javascripts/common/modalInput/password.html',
        '/assets/javascripts/common/modalInput/positiveFloat.html',
        '/assets/javascripts/common/modalInput/select.html',
        '/assets/javascripts/common/modalInput/selectMultiple.html',
        '/assets/javascripts/common/modalInput/textArea.html',
        '/assets/javascripts/common/modalInput/text.html',
        '/assets/javascripts/common/modalInput/url.html',
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');

      this.suiteAddMatchers();
    }));

    beforeEach(function () {
      this.title = this.factory.stringNext();
      this.label = this.factory.stringNext();
    });

    describe('boolean modal', function() {

      beforeEach(function () {
        this.defaultValue = false;
      });

      it('has valid elements and scope', function() {
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

        _.each(inputs, function (input) {
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
        this.options = _.map(_.range(3), function () { return faker.lorem.word(); });
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
        _.each(optionElements, function (optElement) {
          var text = angular.element(optElement).text();
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
        this.options = _.map(_.range(3), function () { return faker.random.word(); });
        this.defaultValue = [ this.options[0] ];
      });

      it('has valid elements and scope', function() {
        var self = this,
            labelElements;

        self.openModal(self.modalInput.selectMultiple,
                       self.defaultValue,
                       self.title,
                       self.label,
                       { selectOptions: self.options });

        expect(self.$document).toHaveModalsOpen(1);

        expect(this.modalElement).toHaveModalTitle(self.title);
        expect(this.modalElement).toHaveLabelStartWith(self.label);
        expect(this.modalElement).toHaveInputs(this.options.length);

        labelElements = this.modalElement.find('form').find('label');
        _.each(labelElements, function (element, index) {
          if (index === 0) { return; } // skip the first label since it's for the group
          expect(self.options).toContain(angular.element(element).text().trim());
        });

        this.dismiss(this.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('form is invalid if a value is required and nothing selected', function() {
        var self = this;

        self.openModal(self.modalInput.selectMultiple,
                       self.defaultValue,
                       self.title,
                       self.label,
                       {
                         required: true,
                         selectOptions: self.options
                       });

        expect(self.$document).toHaveModalsOpen(1);

        expect(this.modalElement).toHaveModalTitle(self.title);
        expect(this.modalElement).toHaveLabelStartWith(self.label);
        expect(this.modalElement).toHaveInputs(this.options.length);

        _.each(this.scope.vm.value, function (value) {
          value.checked = true;
        });
        this.scope.$digest();
        _.each(this.scope.vm.value, function (value) {
          value.checked = false;
        });
        this.scope.$digest();
        this.scope.form.selectValue.$setViewValue(false);
        expect(this.scope.form.$valid).toBe(false);
        expect(this.modalElement).toHaveHelpBlocks();

        this.dismiss(this.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('has a help block when required', function() {
        var self = this;

        self.openModal(self.modalInput.selectMultiple,
                       [],
                       self.title,
                       self.label,
                       {
                         required: true,
                         selectOptions: self.options
                       });

        expect(this.modalElement).toHaveHelpBlocks();

        this.dismiss(this.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
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
        this.defaultValue = this.factory.stringNext();
      });

      it('has valid elements and scope', function() {
        expect(this.$document).toHaveModalsOpen(0);

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
                       this.factory.stringNext(),
                       this.title,
                       this.label);

        expect(this.$document).toHaveModalsOpen(1);
        this.scope.vm.okPressed();
        this.$rootScope.$digest();

        this.$animate.flush();
        this.$rootScope.$digest();
        this.$animate.flush();
        this.$rootScope.$digest();

        expect(this.$document).toHaveModalsOpen(0);
      });

      it('modal should be closed when Cancel button is pressed', function() {
        this.openModal(this.modalInput.text,
                       this.factory.stringNext(),
                       this.title,
                       this.label);

        expect(this.$document).toHaveModalsOpen(1);
        this.scope.vm.closePressed();
        this.$rootScope.$digest();

        this.$animate.flush();
        this.$rootScope.$digest();
        this.$animate.flush();
        this.$rootScope.$digest();

        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('textArea modal', function() {

      beforeEach(function () {
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

});
