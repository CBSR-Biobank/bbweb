/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * @param {object} context The context to use to test the behaviour. See below for required fields.
 *
 * @param {constructor} context.constructor The constructor function for the entity to test.
 *
 * @param {function} context.createFunc The static function that creates the entity name.
 *
 * @param {function} context.restApiUrl A function that returns the URL used to retrieve the entities from
 *        the server.
 *
 * @param {function} context.jsonFactoryFunc The function in factory.js that creates a plain object of the
 * entity to test.
 *
 * @param {function} context.listFunc The static function that invokes the REST API to list the entities.
 *        This function has one parameter: the options object that specifies the query parameters to use
 *        with the URL.
 *
 * @return {null} nothing
 */
function entityInfoCreateSharedBehaviour(context) {

  it('constructor with no parameters has default values', function() {
    var entityName = new context.constructor();
    expect(entityName.id).toBeUndefined();
    expect(entityName.name).toBeUndefined();
  });

  it('fails when creating from an invalid object', function() {
    this.EntityInfo.SCHEMA.required.forEach(function (field) {
      var badEntityJson = _.omit(context.jsonFactoryFunc(), field);

      expect(function () {
        context.createFunc(badEntityJson);
      }).toThrowError(/Missing required property/);
    });
  });

}

/**
 * @param {object} context The context to use to test the behaviour. See below for required fields.
 *
 * @param {constructor} context.constructor The constructor function for the entity to test.
 *
 * @param {function} context.restApiUrl A function that returns the URL used to retrieve the entities from
 *        the server.
 *
 * @param {function} context.jasonFactoryFunc The function in factory.js that creates a plain object of the
 * entity to test.
 *
 * @param {function} context.listFunc The static function that invokes the REST API to list the entities.
 *        This function has one parameter: the options object that specifies the query parameters to use
 *        with the URL.
 *
 * @return {null} nothing
 */
function entityInfoListSharedBehaviour(context) {

  it('can retrieve entity names', function() {
    var names = [ context.jsonFactoryFunc() ];

    this.$httpBackend.whenGET(context.restApiUrl()).respond(this.reply(names));
    context.listFunc().then(testEntity).catch(this.failTest);
    this.$httpBackend.flush();

    function testEntity(reply) {
      expect(reply).toBeArrayOfSize(1);
      expect(reply[0]).toEqual(jasmine.any(context.constructor));
    }
  });

  it('can use options', function() {
    const names = [ context.jsonFactoryFunc() ],
          testEntity = (reply) => {
            expect(reply).toBeArrayOfSize(names.length);
            reply.forEach((entity) => {
              expect(entity).toEqual(jasmine.any(context.constructor));
            });
          },
          optionList = [
            { filter: 'name::test' },
            { sort: 'name' }
          ];

    optionList.forEach((options) => {
      var url = context.restApiUrl() + '?' + this.$httpParamSerializer(options, true);
      this.$httpBackend.whenGET(url).respond(this.reply(names));
      context.listFunc(options).then(testEntity).catch(this.failTest);
      this.$httpBackend.flush();
    });
  });

  it('listing omits empty options', function() {
    var options = { filter: ''},
        names = [ context.jsonFactoryFunc() ],
        testEntity = (reply) => {
          expect(reply).toBeArrayOfSize(names.length);
          reply.forEach((entity) => {
            expect(entity).toEqual(jasmine.any(context.constructor));
          });
        };

    this.$httpBackend.whenGET(context.restApiUrl()).respond(this.reply(names));
    context.listFunc(options).then(testEntity).catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('fails when an invalid entity is returned', function() {
    var names = [ _.omit(context.jsonFactoryFunc(), 'name') ];

    this.$httpBackend.whenGET(context.restApiUrl()).respond(this.reply(names));
    context.listFunc().then(listFail).catch(shouldFail);
    this.$httpBackend.flush();

    function listFail() {
      fail('function should not be called');
    }

    function shouldFail(error) {
      expect(error).toMatch('invalid entity info from server');
    }
  });

}

export  { entityInfoCreateSharedBehaviour, entityInfoListSharedBehaviour }
