/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  const _ = require('lodash');

  /**
   * @param {object} context The context to use to test the behaviour. See below for required fields.
   *
   * @param {constructor} context.constructor The constructor function for the entity to test.
   *
   * @param {function} context.createFunc The static function that creates the entity name.
   *
   * @param {string} context.restApiUrl The URL for the REST API.
   *
   * @param {function} context.factoryFunc The function in factory.js that creates a plain object of the
   * entity to test.
   *
   * @param {function} context.listFunc The static function that invokes the REST API to list the entities.
   *
   * @return {null} nothing
   */
  function entityNameAndStateSharedBehaviour(context) {

    it('constructor with no parameters has default values', function() {
      var entityName = new context.constructor();
      expect(entityName.id).toBeUndefined();
      expect(entityName.name).toBeUndefined();
      expect(entityName.slug).toBeUndefined();
    });

    it('fails when creating from an invalid object', function() {
      this.EntityNameAndState.SCHEMA.required.forEach(function (field) {
        var badEntityJson = _.omit(context.factoryFunc(), field);

        expect(function () {
          context.createFunc(badEntityJson);
        }).toThrowError(/Missing required property/);
      });
    });

    describe('when listing names', function() {

      it('can retrieve entity names', function() {
        var names = [ context.factoryFunc() ];

        this.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
        context.listFunc().then(testEntity).catch(failTest);
        this.$httpBackend.flush();

        function testEntity(reply) {
          expect(reply).toBeArrayOfSize(1);
          expect(reply[0]).toEqual(jasmine.any(context.constructor));
        }
      });

      it('can use options', function() {
        var optionList = [
          { filter: 'name::test' },
          { sort: 'state' }
        ],
            names = [ context.factoryFunc() ];

        optionList.forEach((options) => {
          var url = context.restApiUrl + '?' + this.$httpParamSerializer(options, true);
          this.$httpBackend.whenGET(url).respond(this.reply(names));
          context.listFunc(options).then(testEntity).catch(failTest);
          this.$httpBackend.flush();
        });

        function testEntity(reply) {
          expect(reply).toBeArrayOfSize(names.length);
          reply.forEach((entity) => {
            expect(entity).toEqual(jasmine.any(context.constructor));
          });
        }
      });

      it('listing omits empty options', function() {
        var options = { filter: ''},
            names = [ context.factoryFunc() ],
            testEntity = (reply) => {
              expect(reply).toBeArrayOfSize(names.length);
              reply.forEach((entity) => {
                expect(entity).toEqual(jasmine.any(this.EntityNameAndState));
              });
            };

        this.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
        context.listFunc(options).then(testEntity).catch(failTest);
        this.$httpBackend.flush();
      });

      it('fails when an invalid entity is returned', function() {
        var names = [ _.omit(context.factoryFunc(), 'name') ];

        this.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
        context.listFunc().then(listFail).catch(shouldFail);
        this.$httpBackend.flush();

        function listFail() {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toMatch('Missing required property');
        }
      });

    });

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

  }

  return entityNameAndStateSharedBehaviour;

});
