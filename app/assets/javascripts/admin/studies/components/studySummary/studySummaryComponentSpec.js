/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks                  = require('angularMocks'),
      _                      = require('lodash'),
      entityUpdateSharedSpec = require('../../../../test/entityUpdateSharedSpec');

  describe('Component: studySummary', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createScope = function () {
        var scope = ComponentTestSuiteMixin.prototype.createScope.call(this, { study: this.study });
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      SuiteMixin.prototype.createController = function (enableAllowed) {
        if (_.isUndefined(enableAllowed)) {
          enableAllowed = true;
        }
        this.Study.prototype.isEnableAllowed =
          jasmine.createSpy().and.returnValue(this.$q.when(enableAllowed));

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<study-summary study="vm.study"></study-summary>',
          { study: this.study },
          'studySummary');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      var self = this,
          specimenDescription,
          ceventType;

      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenDescription',
                              'modalService',
                              'notificationsService',
                              'factory');

      specimenDescription = self.factory.collectionSpecimenDescription();
      this.study = this.Study.create(self.factory.study());
      ceventType = self.CollectionEventType.create(
        self.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]}));

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ ceventType ]));
      spyOn(self.modalService, 'showModal').and.returnValue(self.$q.when(true));

      self.study = new self.Study(self.factory.study());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studySummary/studySummary.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/components/statusLine/statusLine.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

    }));

    it('initialization is valid', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
      expect(this.controller.descriptionToggleLength).toBeDefined();
      expect(this.controller.isEnableAllowed).toBeTrue();
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    it('should have valid settings when study has no collection event types', function() {
      this.createController(false);
      expect(this.controller.isEnableAllowed).toBeFalse();
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.Study;
        context.createController   = this.createController.bind(this);
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
      }));

      entityUpdateSharedSpec(context);

    });

    describe('updates to description', function () {
      var context = {};

      beforeEach(inject(function () {
        context.entity             = this.Study;
        context.createController   = this.createController.bind(this);
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
      }));

      entityUpdateSharedSpec(context);

    });

    describe('enabling a study', function() {
      var context = {};

      beforeEach(inject(function () {
        context.state = 'enable';
      }));

      sharedStudyStateBehaviour(context);
    });

    describe('disabling a study', function() {
      var context = {};

      beforeEach(inject(function () {
        context.state = 'disable';
      }));

      sharedStudyStateBehaviour(context);
    });

    describe('retiring a study', function() {
      var context = {};

      beforeEach(inject(function () {
        context.state = 'retire';
      }));

      sharedStudyStateBehaviour(context);
    });

    describe('unretiring a study', function() {
      var context = {};

      beforeEach(inject(function () {
        context.state = 'unretire';
      }));

      sharedStudyStateBehaviour(context);
    });


    function sharedStudyStateBehaviour(context) {

      describe('(shared) study state', function () {

        it('change state', function () {
          spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('ok'));
          spyOn(this.Study, 'get').and.returnValue(this.$q.when(this.study));
          spyOn(this.Study.prototype, context.state).and.returnValue(this.$q.when(this.study));

          this.createController();
          this.controller.changeState(context.state);
          this.scope.$digest();
          expect(this.Study.prototype[context.state]).toHaveBeenCalled();
        });

      });
    }

    it('should throw error for when trying to change to an invalid state', function () {
      var self = this,
          badState = 'xxx';

      this.createController();
      expect(function () {
        self.controller.changeState(badState);
      }).toThrow(new Error('invalid state: ' + badState));
    });
  });

});
