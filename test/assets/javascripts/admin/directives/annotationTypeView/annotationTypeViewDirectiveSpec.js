/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('annotationTypeViewDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<annotation-type-view ',
        '  study="vm.study"',
        '  annotation-type="vm.annotationType"',
        '  return-state="' + this.returnState  + '"',
        '  on-update="vm.onUpdate"',
        '</annotation-type-view>',
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:          this.study,
        annotationType: this.annotationType,
        returnState:    this.returnState,
        onUpdate:       this.onUpdate
      };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('annotationTypeView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'AnnotationType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

      self.returnState      = 'my-return-state';
      self.study            = new self.Study(self.factory.study());
      self.annotationType   = new self.AnnotationType(self.factory.annotationType());
      self.onUpdate         = jasmine.createSpy('onUpdate').and.returnValue(self.$q.when(self.study));
    }));

    it('should have valid scope', function() {
      createController.call(this, this.study, this.annotationType);
      expect(this.controller.annotationType).toBe(this.annotationType);
      expect(this.controller.returnState).toBe(this.returnState);
      expect(this.controller.onUpdate).toBeFunction();
    });

    it('call to back function returns to valid state', function() {
      spyOn(this.$state, 'go').and.returnValue(0);

      createController.call(this, this.study, this.annotationType);
      this.controller.back();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to required', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editRequired';
        context.modalInputFuncName = 'boolean';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('add selections', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'addSelectionOptions';
        context.modalInputFuncName = 'selectMultiple';
      }));

      sharedUpdateBehaviour(context);

    });

    function sharedUpdateBehaviour(context) {

      beforeEach(inject(function () {
        this.injectDependencies('modalInput', 'notificationsService');
      }));

      describe('(shared) update functions', function () {

        it('should update a field', function() {
          var newValue = this.factory.stringNext();

          spyOn(this.modalInput, context.modalInputFuncName).and.returnValue(
            { result: this.$q.when(newValue)});

          createController.call(this);
          expect(this.controller[context.controllerFuncName]).toBeFunction();
          this.controller[context.controllerFuncName]();
          this.scope.$digest();
          expect(this.onUpdate).toHaveBeenCalled();
        });

      });
    }
  });

});
