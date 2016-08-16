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
      sprintf = require('sprintf').sprintf;

  describe('modalInputModule', function() {

    var modalElementFind = function() {
      return this.$document.find('body > div.modal');
    };

    var open = function (modalInputFunc, defaultValue, title, label, options) {
      var modal, modalElement;
      title = title || this.factory.stringNext();
      label = label || this.factory.stringNext();
      modal = modalInputFunc(title, label, defaultValue, options);
      this.$rootScope.$digest();

      modalElement = modalElementFind.call(this);

      return {
        modal:   modal,
        element: modalElement,
        scope:   modalElement.scope()
      };
    };

    var dismiss = function (modal, reason, noFlush) {
      var closed = modal.dismiss(reason);
      this.$rootScope.$digest();
      if (!noFlush) {
        this.$animate.flush();
        this.$rootScope.$digest();
        this.$animate.flush();
        this.$rootScope.$digest();
      }
      return closed;
    };

    function suiteAddMatchers() {
      jasmine.addMatchers({
        toHaveModalsOpen: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var modalDomEls = actual.find('body > div.modal'),
                  pass        = util.equals(modalDomEls.length, expected, customEqualityTesters),
                  message     = sprintf('Expected "%s" %s have "%s" modals opened.',
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
                  message = sprintf('Expected "%s" %s have title be "%s"',
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
              message = sprintf('Expected "%s" %s have label be "%s"',
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
                  message = sprintf('Expected input element %s be valid',
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
              message = sprintf('Expected "%s" %s have %d input elements',
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
              message = sprintf('Expected "%s"" type %s be "%s"',
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
              message = sprintf('Expected modal %s have a textarea element',
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
        toHaveHelpBlocks: function(util, customEqualityTesters) {
          return {
            compare: function(actual, expected) {
              var element = actual.find('form').find('.help-block'),
                  pass,
                  message;

              expected = expected || '';
              pass = (element.length > 0);
              message = sprintf('Expected modal %s have help blocks', pass ? 'not to' : 'to');

              return { pass: pass, message: message };
            }
          };
        }
      });
    }

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$animate',
                              '$document',
                              'modalInput',
                              'factory');

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

      suiteAddMatchers();
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
        var modalInfo = open.call(this,
                                  this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(2);
        expect(modalInfo.element).toHaveValuesInControllerScope(
          { value: this.defaultValue, options: undefined });

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when value is required and both values are unchecked', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        modalInfo.scope.form.value.$setViewValue(null);
        expect(modalInfo.scope.form.$valid).toBe(false);

        // check that it becomes valid
        modalInfo.scope.form.value.$setViewValue(false);
        expect(modalInfo.scope.form.$valid).toBe(true);

        dismiss.call(this, modalInfo.modal, 'closed in test');
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
        var modalInfo = open.call(this,
                                  this.modalInput.dateTime,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputs(1);
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: undefined });
        expect(modalInfo.element).not.toHaveHelpBlocks();

        expect(modalInfo.element.scope().vm.value).toBeDate();
        expect(new Date(modalInfo.element.scope().vm.value)).toEqual(this.date);

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when value is required and value is blank', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.boolean,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });

        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('email modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.internet.email();
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.email,
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
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.email,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid email', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.email,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('natural number modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.naturalNumber,
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
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.naturalNumber,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid number', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.naturalNumber,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('-1');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('number modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.number,
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
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.number,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is not a number', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.number,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('password modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var inputs,
            modalInfo = open.call(this,
                                  this.modalInput.password,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveInputs(3);
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: { } });
        expect(modalInfo.element).not.toHaveHelpBlocks();

        inputs = modalInfo.element.find('form').find('input');
        expect(inputs.attr('focus-me')).toBe('true');

        _.each(inputs, function (input) {
          expect(angular.element(input).attr('type')).toBe('password');
        });

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when new password and confirm password do not match', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.password,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.newPassword.$setViewValue('abcabcabc');
        modalInfo.scope.form.confirmPassword.$setViewValue('xyzxyzxyz');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('positive float modal', function() {

      beforeEach(function () {
        this.defaultValue = 10;
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.positiveFloat,
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
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is not a number', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is a negative number', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.positiveFloat,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('-1.00');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
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
            modalInfo = open.call(self,
                                  self.modalInput.select,
                                  self.defaultValue,
                                  self.title,
                                  self.label,
                                  { selectOptions: self.options });

        expect(self.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(self.title);
        expect(modalInfo.element).toHaveLabelStartWith(self.label);
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: self.defaultValue });
        expect(modalInfo.element).not.toHaveHelpBlocks();

        optionElements = modalInfo.element.find('form').find('option');
        _.each(optionElements, function (optElement) {
          var text = angular.element(optElement).text();
          if (text !== '-- make a selection --') {
            expect(self.options).toContain(text);
          }
        });

        dismiss.call(self, modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when a value is required and nothing selected', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.select,
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

        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
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
            modalInfo = open.call(self,
                                  self.modalInput.selectMultiple,
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

        dismiss.call(self, modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('form is invalid if a value is required and nothing selected', function() {
        var self = this,
            modalInfo = open.call(self,
                                  self.modalInput.selectMultiple,
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
          value.checked = true;
        });
        modalInfo.scope.$digest();
        _.each(modalInfo.scope.vm.value, function (value) {
          value.checked = false;
        });
        modalInfo.scope.$digest();
        modalInfo.scope.form.selectValue.$setViewValue(false);
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(self, modalInfo.modal, 'closed in test');
        expect(self.$document).toHaveModalsOpen(0);
      });

      it('has a help block when required', function() {
        var self = this,
            modalInfo = open.call(self,
                                  self.modalInput.selectMultiple,
                                  [],
                                  self.title,
                                  self.label,
                                  {
                                    required: true,
                                    selectOptions: self.options
                                  });

        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(self, modalInfo.modal, 'closed in test');
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
        var modalInfo = open.call(this,
                                  this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveInputElementBeFocused();
        expect(modalInfo.element).toHaveInputElementTypeAttrBe('text');
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when minimum length is not met', function() {
        var minLength = 2,
            modalInfo = open.call(this,
                                  this.modalInput.text,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { minLength: minLength });

        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { minLength: minLength } });
        modalInfo.scope.form.value.$setViewValue('x');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('modal should be closed when OK button is pressed', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.text,
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
        var modalInfo = open.call(this,
                                  this.modalInput.text,
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

    describe('textArea modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.lorem.sentences(4);
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.textArea,
                                  this.defaultValue,
                                  this.title,
                                  this.label);

        expect(this.$document).toHaveModalsOpen(1);

        expect(modalInfo.element).toHaveTitle(this.title);
        expect(modalInfo.element).toHaveLabelStartWith(this.label);
        expect(modalInfo.element).toHaveValidTextAreaElement();
        expect(modalInfo.element).toHaveValuesInControllerScope({ value: this.defaultValue });
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.textArea,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

    describe('url modal', function() {

      beforeEach(function () {
        this.defaultValue = faker.image.imageUrl();
      });

      it('has valid elements and scope', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.url,
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
        expect(modalInfo.element).not.toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when using required option and input is empty', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.url,
                                  this.defaultValue,
                                  this.title,
                                  this.label,
                                  { required: true });
        expect(modalInfo.element).toHaveValuesInControllerScope({ options: { required: true } });
        modalInfo.scope.form.value.$setViewValue('');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this, modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('form is invalid when input is an invalid email', function() {
        var modalInfo = open.call(this,
                                  this.modalInput.url,
                                  this.defaultValue,
                                  this.title,
                                  this.label);
        modalInfo.scope.form.value.$setViewValue('xxx');
        expect(modalInfo.scope.form.$valid).toBe(false);
        expect(modalInfo.element).toHaveHelpBlocks();

        dismiss.call(this,  modalInfo.modal, 'closed in test');
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

  });

});
