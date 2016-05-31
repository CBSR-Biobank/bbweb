/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'moment',
  'faker',
  'sprintf'
], function(angular, mocks, _, moment, faker, sprintf) {
  'use strict';

  describe('modalInputModule', function() {

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$rootScope   = self.$injector.get('$rootScope');
      self.$animate     = self.$injector.get('$animate');
      self.$document    = self.$injector.get('$document');
      self.modalInput   = self.$injector.get('modalInput');
      self.factory = self.$injector.get('factory');

      jasmine.addMatchers({
        toHaveModalsOpen: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var modalDomEls = actual.find('body > div.modal'),
                  pass        = util.equals(modalDomEls.length, expected, customEqualityTesters),
                  message     = sprintf.sprintf('Expected "%s" %s have "%s" modals opened.',
                                                angular.mock.dump(modalDomEls),
                                                pass ? 'not to' : 'to',
                                                expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveTitle: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('.modal-title'),
                  pass    = util.equals(element.text(), expected, customEqualityTesters),
                  message = sprintf.sprintf('Expected "%s" %s have title be "%s"',
                                            angular.mock.dump(element),
                                            pass ? 'not to' : 'to',
                                            expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveLabelStartWith: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('label'),
                  pass,
                  message;

              expected = expected || '';
              pass    = element.text().slice(0, expected.length) === expected;
              message = sprintf.sprintf('Expected "%s" %s have label be "%s"',
                                        angular.mock.dump(element),
                                        pass ? 'not to' : 'to',
                                        expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputElementBeFocused: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('input'),
                  pass = (element.length === 1) && (element.attr('focus-me') === 'true'),
                  message = sprintf.sprintf('Expected input element %s be valid',
                                            angular.mock.dump(element),
                                            pass ? 'not to' : 'to',
                                            expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputs: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var pass, message, element = actual.find('form').find('input');
              expected = expected || 0;
              pass = (element.length === expected);
              message = sprintf.sprintf('Expected "%s" %s have %d input elements',
                                        angular.mock.dump(element),
                                        pass ? 'not to' : 'to',
                                        expected);
              return { pass: pass, message: message };
            }
          };
        },
        toHaveInputElementTypeAttrBe: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('input'),
                  pass,
                  message;

              expected = expected || '';
              pass = (element.length === 1) && (element.attr('type') === expected);
              message = sprintf.sprintf('Expected "%s"" type %s be "%s"',
                                        angular.mock.dump(element),
                                        pass ? 'not to' : 'to',
                                        expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveValidTextAreaElement: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('textarea'),
                  pass,
                  message;

              pass = element.length === 1 &&
                (element.attr('focus-me') === 'true') &&
                (element.attr('ng-model') === 'vm.value') &&
                (element.attr('ng-required') === 'vm.options.required');
              message = sprintf.sprintf('Expected modal %s have a textarea element',
                                        angular.mock.dump(element),
                                        pass ? 'not to' : 'to',
                                        expected);

              return { pass: pass, message: message };
            }
          };
        },
        toHaveValuesInControllerScope: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var scope = actual.scope().vm,
                  pass,
                  message;

              expected = expected || {};
              pass = _.chain(expected).keys().every(checkScopeValue).value();
              message = sprintf.sprintf('Expected modal controller scope "%s" %s have a values "%s"',
                                        angular.mock.dump(scope),
                                        pass ? 'not to' : 'to',
                                        angular.mock.dump(expected));

              return { pass: pass, message: message };

              function checkScopeValue(key) {
                return _.has(scope, key) && _.isEqual(expected[key], scope[key]);
              }
            }
          };
        }
      });

      self.putHtmlTemplates(
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
        '/assets/javascripts/common/modalInput/url.html');

      this.open = open;
      this.modalElement = modalElement;
      this.controller = controller;
      this.dismiss = dismiss;

      //--

      function open(modalInputFunc, defaultValue, title, label, options) {
        var modal, modalElement;
        title = title || self.factory.stringNext();
        label = label || self.factory.stringNext();
        modal = modalInputFunc(title, label, defaultValue, options);
        self.$rootScope.$digest();

        modalElement = self.modalElement();

        return {
          modal:   modal,
          element: modalElement,
          scope:   modalElement.scope()
        };
      }

      function modalElement() {
        return self.$document.find('body > div.modal');
      }

      function controller() {
        return modalElement().scope().vm;
      }

      function dismiss(modal, reason, noFlush) {
        var closed = modal.dismiss(reason);
        self.$rootScope.$digest();
        if (!noFlush) {
          self.$animate.flush();
          self.$rootScope.$digest();
          self.$animate.flush();
          self.$rootScope.$digest();
        }
        return closed;
      }

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
        var modalInfo = this.open(this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(2);
        expect(modalInfo.element).toHaveValuesInControllerScope(
          { value: this.defaultValue, options: undefined });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when value is required and both values are unchecked', function() {
        var modalInfo = this.open(this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        modalInfo.scope.form.value.$setViewValue(null);
        expect(modalInfo.scope.form.$valid).toBe(false);

        // check that it becomes valid
        modalInfo.scope.form.value.$setViewValue(false);
        expect(modalInfo.scope.form.$valid).toBe(true);

        this.dismiss(modalInfo.modal, 'closed in test');
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
        var modalInfo = this.open(this.modalInput.dateTime,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: undefined });

        expect(modalInfo.element.scope().vm.value).toBeDate();
        expect(new Date(modalInfo.element.scope().vm.value)).toEqual(this.date);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when value is required and value is blank', function() {
        var modalInfo = this.open(this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });

        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('email modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.internet.email();
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.email,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('email');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.email,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid email', function() {
        var modalInfo = this.open(this.modalInput.email,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('natural number modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.naturalNumber,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('number');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.naturalNumber,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid number', function() {
        var modalInfo = this.open(this.modalInput.naturalNumber,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('-1');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('number modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.number,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('number');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.number,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is not a number', function() {
        var modalInfo = this.open(this.modalInput.number,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('password modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var inputs,
            modalInfo = this.open(this.modalInput.password,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveInputs(3);
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: { } });

        inputs = modalInfo.element.find('form').find('input');
        expect(inputs.attr('focus-me')).toBe('true');

        _.each(inputs, function (input) {
          expect(angular.element(input).attr('type')).toBe('password');
        });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when new password and confirm password do not match', function() {
        var modalInfo = this.open(this.modalInput.password,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.newPassword.$setViewValue('abcabcabc');
        modalInfo.scope.form.confirmPassword.$setViewValue('xyzxyzxyz');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('positive float modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('number');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is not a number', function() {
        var modalInfo = this.open(this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is a negative number', function() {
        var modalInfo = this.open(this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('-1.00');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
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
            optionElements,
            modalInfo = self.open(self.modalInput.select,
                                  self.defaultValue,
                                  self.title,
                                  self.label,
                                  { selectOptions: self.options });

        expect(self.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(self.title);
        expect(modalInfo.element).toHaveLabelStartWith(self.label);
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: self.defaultValue });

        optionElements = modalInfo.element.find('form').find('option');
        _.each(optionElements, function (optElement) {
          var text = angular.element(optElement).text();
          if (text !== '-- make a selection --') {
            expect(self.options).toContain(text);
          }
        });

        self.dismiss(modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when a value is required and nothing selected', function() {
        var modalInfo = this.open(this.modalInput.select,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  {
                                    required: true,
                                    selectOptions: this.options
                                  });

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        modalInfo.scope.vm.value = '';
        modalInfo.scope.$digest();
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
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
            labelElements,
            modalInfo = self.open(self.modalInput.selectMultiple,
                                  self.defaultValue,
                                  self.title,
                                  self.label,
                                  { selectOptions: self.options });

        expect(self.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(self.title);
        expect(modalInfo.element).toHaveLabelStartWith(self.label);
        expect(modalInfo.element).toHaveInputs(this.options.length);

        labelElements = modalInfo.element.find('form').find('label');
        _.each(labelElements, function (element, index) {
          if (index === 0) { return; } // skip the first label since it's for the group
          expect(self.options).toContain(angular.element(element).text().trim());
        });

        self.dismiss(modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('form is invalid if a value is required and nothing selected', function() {
        var self = this,
            modalInfo = self.open(self.modalInput.selectMultiple,
                                  self.defaultValue,
                                  self.title,
                                  self.label,
                                  {
                                    required: true,
                                    selectOptions: self.options
                                  });

        expect(self.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(self.title);
        expect(modalInfo.element).toHaveLabelStartWith(self.label);
        expect(modalInfo.element).toHaveInputs(this.options.length);

        _.each(modalInfo.scope.vm.value, function (value) {
          value.checked = false;
        });
        modalInfo.scope.$digest();
        expect(modalInfo.scope.form.$valid).toBe(false);

        self.dismiss(modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('throws an exception if default value is not an array', function() {
        var self = this;

        expect(function () {
          self.modalInput.selectMultiple('', '', '', { });
          self.$rootScope.$digest();
        }).toThrowError(/select options not provided/);
      });

      it('throws an exception if default value is not an array', function() {
        var self = this;

        expect(function () {
          self.modalInput.selectMultiple('', '', '', { selectOptions: self.options });
          self.$rootScope.$digest();
        }).toThrowError(/defaultValue is not an array/);
      });

    });

    describe('text modal', function() {

      beforeEach(function () {
        this.defaultValue = this.factory.stringNext();
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('text');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when minimum length is not met', function() {
        var minLength = 2,
            modalInfo = this.open(this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { minLength: minLength });

        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { minLength: minLength } });
        modalInfo.scope.form.value.$setViewValue('x');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('textArea modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.lorem.sentences(4);
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.textArea,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveValidTextAreaElement();
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

     it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.textArea,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('url modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.image.imageUrl();
      });

      it('has valid elements and scope', function() {
        var modalInfo = this.open(this.modalInput.url,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('url');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = this.open(this.modalInput.url,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid email', function() {
        var modalInfo = this.open(this.modalInput.url,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);

        this.dismiss(modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('textArea modal', function() {

      it('modal should be closed when OK button is pressed', function() {
        var modalInfo = this.open(this.modalInput.text,
                                  this.factory.stringNext(),
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.vm.okPressed();
        this.$rootScope.$digest();

        this.$animate.flush();
        this.$rootScope.$digest();
        this.$animate.flush();
        this.$rootScope.$digest();

        expect(this.$document).toHaveModalsOpen(0);
      });

      it('modal should be closed when Cancel button is pressed', function() {
        var modalInfo = this.open(this.modalInput.text,
                                  this.factory.stringNext(),
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);
        modalInfo.scope.vm.closePressed();
        this.$rootScope.$digest();

        this.$animate.flush();
        this.$rootScope.$digest();
        this.$animate.flush();
        this.$rootScope.$digest();

        expect(this.$document).toHaveModalsOpen(0);
      });

    });

  });

});
