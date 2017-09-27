/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks                  = require('angularMocks'),
      _                      = require('lodash'),
      entityUpdateSharedSpec = require('../../../../test/entityUpdateSharedSpec');

  describe('ceventTypeViewComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (study, collectionEventType) {
        study = study || this.study;
        collectionEventType = collectionEventType || this.collectionEventType;

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType));

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<cevent-type-view study="vm.study" collection-event-type="vm.ceventType"></cevent-type-view>',
          {
            study:      study,
            ceventType: collectionEventType
          },
          'ceventTypeView');
      };

      return SuiteMixin;
    }


    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'StudyState',
                              'CollectionEventType',
                              'CollectionSpecimenDescription',
                              'AnnotationType',
                              'notificationsService',
                              'domainNotificationService',
                              'modalService',
                              'factory');

      this.jsonStudy              = this.factory.study();
      this.jsonCet                = this.factory.collectionEventType(this.jsonStudy);
      this.study                  = new this.Study(this.jsonStudy);
      this.collectionEventType    = new this.CollectionEventType(this.jsonCet);

      spyOn(this.$state, 'go').and.returnValue(null);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/ceventTypeView/ceventTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html',
        '/assets/javascripts/admin/studies/components/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummary.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html',
        '/assets/javascripts/common/components/statusLine/statusLine.html',
        '/assets/javascripts/common/modalInput/modalInput.html');
    }));

    it('scope should be valid', function() {
      this.createController();
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);
    });

    it('calling addAnnotationType should change to the correct state', function() {
      this.createController();
      this.controller.addAnnotationType();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
    });

    it('calling addSpecimenDescription should change to the correct state', function() {
      this.createController();
      this.controller.addSpecimenDescription();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd');
    });

    it('calling editAnnotationType should change to the correct state', function() {
      var annotType = new this.AnnotationType(this.factory.annotationType());

      this.createController();
      this.controller.editAnnotationType(annotType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        { annotationTypeId: annotType.id });
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        var self = this;
        context.entity             = this.CollectionEventType;
        context.createController   = function () { self.createController(); };
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
        var self = this;
        context.entity             = this.CollectionEventType;
        context.createController   = function () { self.createController(); };
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
        var self = this;
        context.entity             = this.CollectionEventType;
        context.createController   = function () { self.createController(); };
        context.updateFuncName     = 'updateRecurring';
        context.controllerFuncName = 'editRecurring';
        context.modalInputFuncName = 'boolean';
        context.newValue           = false;
      }));

      entityUpdateSharedSpec(context);

    });

    it('editing a specimen description changes to correct state', function() {
      var specimenDescription =
          new this.CollectionSpecimenDescription(this.factory.collectionSpecimenDescription());

      this.createController();
      this.controller.editSpecimenDescription(specimenDescription);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType.specimenDescriptionView',
        { specimenDescriptionId: specimenDescription.id });
    });

    describe('removing a specimen description', function() {

      it('can be removed when in valid state', function() {
        var modalService = this.$injector.get('modalService'),
            jsonSpecimenDescription = this.factory.collectionSpecimenDescription(),
            jsonCeventType = this.factory.collectionEventType(
              { specimenDescriptions: [ jsonSpecimenDescription ]}),
            ceventType = this.CollectionEventType.create(jsonCeventType);

        spyOn(modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.domainNotificationService, 'removeEntity').and.callThrough();
        spyOn(this.CollectionEventType.prototype, 'removeSpecimenDescription')
          .and.returnValue(this.$q.when(ceventType));

        this.createController();
        this.controller.modificationsAllowed = true;
        this.controller.removeSpecimenDescription(ceventType.specimenDescriptions[0]);
        this.scope.$digest();

        expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
        expect(this.CollectionEventType.prototype.removeSpecimenDescription).toHaveBeenCalled();
      });

      it('throws an error if study is not disabled', function() {
        var self = this,
            specimenDescription = new self.CollectionSpecimenDescription(
              self.factory.collectionSpecimenDescription());

        spyOn(self.domainNotificationService, 'removeEntity').and.returnValue(self.$q.when('OK'));

        _([self.StudyState.ENABLED, self.StudyState.RETIRED]).forEach(function (state) {
          self.study.state = state;
          self.createController();
          expect(function () {
            self.controller.removeSpecimenDescription(specimenDescription);
          }).toThrowError('modifications not allowed');
        });
      });

    });

    describe('removing an annotation type', function() {

      it('can be removed when in valid state', function() {
        var modalService = this.$injector.get('modalService'),
            jsonAnnotType = this.factory.annotationType(),
            jsonCeventType = this.factory.collectionEventType({ annotationTypes: [ jsonAnnotType ]}),
            ceventType = this.CollectionEventType.create(jsonCeventType);

        spyOn(modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.CollectionEventType.prototype, 'removeAnnotationType')
          .and.returnValue(this.$q.when(ceventType));

        this.createController();
        this.controller.annotationTypeIdsInUse = [];
        this.controller.removeAnnotationType(ceventType.annotationTypes[0]);
        this.scope.$digest();

        expect(modalService.modalOkCancel).toHaveBeenCalled();
        expect(this.CollectionEventType.prototype.removeAnnotationType).toHaveBeenCalled();
      });

      it('throws an error if modifications are not allowed', function() {
        var annotationType = new this.AnnotationType(this.factory.annotationType());

        spyOn(this.modalService, 'modalOk').and.returnValue(null);
        spyOn(this.CollectionEventType.prototype, 'removeAnnotationType').and.callThrough();

        this.createController();
        this.controller.annotationTypeIdsInUse = [ annotationType.id ];
        this.controller.removeAnnotationType(annotationType);

        expect(this.modalService.modalOk).toHaveBeenCalled();
        expect(this.CollectionEventType.prototype.removeAnnotationType).not.toHaveBeenCalled();
      });

      it('throws an error if study is not disabled', function() {
        var self = this,
            annotationType = new this.AnnotationType(this.factory.annotationType());

        spyOn(self.domainNotificationService, 'removeEntity').and.returnValue(self.$q.when('OK'));

        _([self.StudyState.ENABLED, self.StudyState.RETIRED]).forEach(function (state) {
          self.createController();
          self.study.state = state;

          expect(function () {
            self.controller.removeAnnotationType(annotationType);
          }).toThrowError('modifications not allowed');
        });
      });

    });

    it('updates state when panel button is clicked', function() {
      var panelState;

      this.createController();
      panelState = this.controller.isPanelCollapsed;
      this.controller.panelButtonClicked();
      this.scope.$digest();

      expect(this.controller.isPanelCollapsed).not.toEqual(panelState);
    });

    describe('removing a collection event type', function() {

      it('can remove the collection event type', function() {
        spyOn(this.CollectionEventType.prototype, 'inUse').and.returnValue(this.$q.when(false));
        spyOn(this.CollectionEventType.prototype, 'remove').and.returnValue(this.$q.when(true));

        spyOn(this.domainNotificationService, 'removeEntity').and.callThrough();
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.notificationsService, 'success').and.returnValue(null);

        this.createController();
        this.controller.removeCeventType();
        this.scope.$digest();

        expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection',
                                                    {},
                                                    { reload: true });
      });

      it('user is informed if it cannot be removed', function() {
        spyOn(this.CollectionEventType.prototype, 'inUse').and.returnValue(this.$q.when(true));
        spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

        this.createController();
        this.controller.removeCeventType();
        this.scope.$digest();

        expect(this.modalService.modalOk).toHaveBeenCalled();
      });


    });

  });

});
