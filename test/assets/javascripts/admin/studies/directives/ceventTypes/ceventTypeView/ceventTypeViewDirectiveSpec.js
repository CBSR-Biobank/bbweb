/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                = require('angular'),
      mocks                  = require('angularMocks'),
      _                      = require('lodash'),
      entityUpdateSharedSpec = require('../../../../../test/entityUpdateSharedSpec');

  describe('ceventTypeViewDirective', function() {

    var createController = function () {
        this.element = angular.element(
          '<cevent-type-view study="vm.study" cevent-type="vm.ceventType"></cevent-type-view>');

        this.scope = this.$rootScope.$new();
        this.scope.vm = {
          study: this.study,
          ceventType: this.collectionEventType
        };
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('ceventTypeView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testUtils, TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'StudyStatus',
                              'CollectionEventType',
                              'CollectionSpecimenSpec',
                              'AnnotationType',
                              'notificationsService',
                              'domainNotificationService',
                              'factory');

      self.jsonStudy              = this.factory.study();
      self.jsonCet                = self.factory.collectionEventType(self.jsonStudy);
      self.study                  = new self.Study(self.jsonStudy);
      self.collectionEventType    = new self.CollectionEventType(self.jsonCet);

      spyOn(this.$state, 'go').and.returnValue(true);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/ceventTypeView/ceventTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html',
        '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecSummary/collectionSpecimenSpecSummary.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html',
        '/assets/javascripts/common/modalInput/modalInput.html');
    }));

    it('scope should be valid', function() {
      createController.call(this);
      expect(this.controller.ceventType).toBe(this.collectionEventType);
    });

    it('calling addAnnotationType should change to the correct state', function() {
      createController.call(this);
      this.controller.addAnnotationType();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
    });

    it('calling addSpecimenSpec should change to the correct state', function() {
      createController.call(this);
      this.controller.addSpecimenSpec();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.specimenSpecAdd');
    });

    it('calling editAnnotationType should change to the correct state', function() {
      var annotType = new this.AnnotationType(this.factory.annotationType());

      createController.call(this);
      this.controller.editAnnotationType(annotType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        { annotationTypeId: annotType.uniqueId });
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.CollectionEventType;
        context.createController   = createController;
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
        context.newValue           = this.factory.stringNext();
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to description', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.CollectionEventType;
        context.createController   = createController;
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
        context.newValue           = this.factory.stringNext();
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to recurring', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity               = this.CollectionEventType;
        context.createController   = createController;
        context.updateFuncName       = 'updateRecurring';
        context.controllerFuncName   = 'editRecurring';
        context.modalInputFuncName = 'boolean';
        context.newValue             = false;
      }));

      entityUpdateSharedSpec(context);

    });

    it('editing a specimen spec changes to correct state', function() {
      var specimenSpec = new this.CollectionSpecimenSpec(this.factory.collectionSpecimenSpec());

      createController.call(this);
      this.controller.editSpecimenSpec(specimenSpec);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType.specimenSpecView',
        { specimenSpecId: specimenSpec.uniqueId });
    });

    describe('removing a specimen spec', function() {

      it('can be removed when in valid state', function() {
        var modalService = this.$injector.get('modalService'),
            jsonSpecimenSpec = this.factory.collectionSpecimenSpec(),
            jsonCeventType = this.factory.collectionEventType({ specimenSpecs: [ jsonSpecimenSpec ]}),
            ceventType = new this.CollectionEventType(jsonCeventType);

        spyOn(modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.domainNotificationService, 'removeEntity').and.callThrough();
        spyOn(this.CollectionEventType.prototype, 'removeSpecimenSpec')
          .and.returnValue(this.$q.when(ceventType));

        createController.call(this);
        this.controller.modificationsAllowed = true;
        this.controller.removeSpecimenSpec(ceventType.specimenSpecs[0]);
        this.scope.$digest();

        expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
        expect(this.CollectionEventType.prototype.removeSpecimenSpec).toHaveBeenCalled();
      });

      it('throws an error if study is not disabled', function() {
        var self = this,
            specimenSpec = new self.CollectionSpecimenSpec(self.factory.collectionSpecimenSpec());

        spyOn(self.domainNotificationService, 'removeEntity').and.returnValue(self.$q.when('OK'));

        _([self.StudyStatus.ENABLED, self.StudyStatus.RETIRED]).forEach(function (status) {
          self.study.status = status;
          createController.call(self);
          expect(function () {
            self.controller.removeSpecimenSpec(specimenSpec);
          }).toThrowError('modifications not allowed');
        });
      });

    });

    describe('removing an annotation type', function() {

      it('can be removed when in valid state', function() {
        var modalService = this.$injector.get('modalService'),
            jsonAnnotType = this.factory.annotationType(),
            jsonCeventType = this.factory.collectionEventType({ annotationTypes: [ jsonAnnotType ]}),
            ceventType = new this.CollectionEventType(jsonCeventType);

        spyOn(modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.CollectionEventType.prototype, 'removeAnnotationType')
          .and.returnValue(this.$q.when(ceventType));

        createController.call(this);
        this.controller.annotationTypeIdsInUse = [];
        this.controller.removeAnnotationType(ceventType.annotationTypes[0]);
        this.scope.$digest();

        expect(modalService.modalOkCancel).toHaveBeenCalled();
        expect(this.CollectionEventType.prototype.removeAnnotationType).toHaveBeenCalled();
      });

      it('throws an error if modifications are not allowed', function() {
        var annotationType = new this.AnnotationType(this.factory.annotationType());

        spyOn(this.CollectionEventType.prototype, 'removeAnnotationType').and.callThrough();

        createController.call(this);
        this.controller.annotationTypeIdsInUse = [ annotationType.uniqueId ];
        this.controller.removeAnnotationType(annotationType);

        expect(this.CollectionEventType.prototype.removeAnnotationType).not.toHaveBeenCalled();
      });

      it('throws an error if study is not disabled', function() {
        var self = this,
            annotationType = new this.AnnotationType(this.factory.annotationType());

        spyOn(self.domainNotificationService, 'removeEntity').and.returnValue(self.$q.when('OK'));

        _([self.StudyStatus.ENABLED, self.StudyStatus.RETIRED]).forEach(function (status) {
          createController.call(self);
          self.study.status = status;

          expect(function () {
            self.controller.removeAnnotationType(annotationType);
          }).toThrowError('modifications not allowed');
        });
      });

    });

    it('updates state when panel button is clicked', function() {
      var panelState;

      createController.call(this);
      panelState = this.controller.isPanelCollapsed;
      this.controller.panelButtonClicked();
      this.scope.$digest();

      expect(this.controller.isPanelCollapsed).not.toEqual(panelState);
    });

  });

});
