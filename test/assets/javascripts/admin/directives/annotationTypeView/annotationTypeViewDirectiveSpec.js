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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('annotationTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q             = self.$injector.get('$q');
      self.Study          = self.$injector.get('Study');
      self.AnnotationType = self.$injector.get('AnnotationType');
      self.factory   = self.$injector.get('factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

      self.createController = createController;
      self.returnState      = 'my-return-state';
      self.study            = new self.Study(self.factory.study());
      self.annotationType   = new self.AnnotationType(self.factory.annotationType());
      self.onUpdate         = jasmine.createSpy('onUpdate').and.returnValue(self.$q.when(self.study));

      function createController() {
        self.element = angular.element([
          '<annotation-type-view ',
          '  study="vm.study"',
          '  annotation-type="vm.annotationType"',
          '  return-state="' + self.returnState  + '"',
          '  on-update="vm.onUpdate"',
          '</annotation-type-view>',
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = {
          study:          self.study,
          annotationType: self.annotationType,
          returnState:    self.returnState,
          onUpdate:       self.onUpdate
        };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('annotationTypeView');
      }
    }));

    it('should have valid scope', function() {
      this.createController(this.study, this.annotationType);
      expect(this.controller.annotationType).toBe(this.annotationType);
      expect(this.controller.returnState).toBe(this.returnState);
      expect(this.controller.onUpdate).toBeFunction();
    });

    it('call to back function returns to valid state', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.returnValue(0);

      this.createController(this.study, this.annotationType);
      this.controller.back();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
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
        this.modalInput           = this.$injector.get('modalInput');
        this.notificationsService = this.$injector.get('notificationsService');
      }));

      describe('(shared) update functions', function () {

        it('should update a field', function() {
          var newValue = this.factory.stringNext(),
              deferred = this.$q.defer();

          spyOn(this.modalInput, context.modalInputFuncName).and.returnValue({ result: deferred.promise });
          deferred.resolve(newValue);

          this.createController();
          expect(this.controller[context.controllerFuncName]).toBeFunction();
          this.controller[context.controllerFuncName]();
          this.scope.$digest();
          expect(this.onUpdate).toHaveBeenCalled();
        });

      });
    }
  });

});
